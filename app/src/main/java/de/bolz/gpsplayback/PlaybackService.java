package de.bolz.gpsplayback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Created by johannes on 14.03.18.
 */

public class PlaybackService extends Service {

    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String NOTIFICATION_CHANNEL_ID = "de.bolz.gpsplayback.NOTIFICATION_CHANNEL";

    private LocationSource locationSource;
    private final BlockingQueue<Location> locationQueue = new ArrayBlockingQueue<>(1000);
    private Handler mockLocationThreadHandler;
    private LocationManager locationManager;

    private long firstLocationTimestamp = -1l;
    private long playbackStartedAt;
    private long lastPostTime;

    static void start(Context context) {
        context.startService(new Intent(ACTION_START, null, context, PlaybackService.class));
    }

    static void stop(Context context) {
        context.startService(new Intent(ACTION_STOP, null, context, PlaybackService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_START:
                    start();
                    break;
                case ACTION_STOP:
                    stop();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void start() {
        putInForeground();
        locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false,
                true, true, true, Criteria.POWER_HIGH, Criteria.ACCURACY_HIGH);
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        startListening();
        startReading();
    }

    private void stop() {
        locationSource.stop();
        stopMe();
    }

    private void startReading() {
        locationSource = new JsonLocationSource(LocationManager.GPS_PROVIDER, locationListener, new File(Environment.getExternalStorageDirectory(), "track.json"));
        new Thread() {
            @Override
            public void run() {
                try {
                    locationSource.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    stopSelf();
                }
            }
        }.start();
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onNewLocation(Location location) {
            handleLocation(location);
        }

        @Override
        public void onFinished() {
            long delay = lastPostTime - SystemClock.elapsedRealtime() + 100;
            mockLocationThreadHandler.postDelayed(() -> stopMe(), delay);
            return;
        }
    };

    private void handleLocation(Location location) {
        try {
            locationQueue.put(location);
            scheduleLocation(location);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void startListening() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mockLocationThreadHandler = new Handler(Looper.myLooper());
                countDownLatch.countDown();
                Looper.loop();
            }
        }.start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void scheduleLocation(Location location) {
        long locationTime = location.getTime();
        long now = SystemClock.elapsedRealtime();
        if (firstLocationTimestamp == -1l) {
            firstLocationTimestamp = locationTime;
            playbackStartedAt = now;
        }
        long absoluteTimeToPostLocation = locationTime + (playbackStartedAt - firstLocationTimestamp);
        long postDelay = absoluteTimeToPostLocation - now;

        System.out.println("##### postDelay = " + postDelay + ", absolute post time = " + absoluteTimeToPostLocation);
        mockLocationThreadHandler.postDelayed(() -> {
            try {
                Location locationFromQueue = locationQueue.take();
                locationFromQueue.setTime(System.currentTimeMillis());
                System.out.println("##### posting " + locationFromQueue);
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, locationFromQueue);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }, postDelay);
        lastPostTime = absoluteTimeToPostLocation;
    }

    private void putInForeground() {
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_NONE);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
            notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID).build();
        } else {
            notification = new Notification.Builder(this).build();
        }
        startForeground(250313, notification);
    }

    private void stopMe() {
        System.out.println("##### stopMe()");
        mockLocationThreadHandler.getLooper().quit();
        firstLocationTimestamp = -1l;
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false);
        locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        stopForeground(true);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
