package de.bolz.gpsplayback.playback;

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

import javax.inject.Inject;

import de.bolz.gpsplayback.App;
import de.bolz.gpsplayback.AppExecutors;
import de.bolz.gpsplayback.R;

/**
 * Created by johannes on 14.03.18.
 */

public class PlaybackService extends Service {

    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String NOTIFICATION_CHANNEL_ID = "de.bolz.gpsplayback.NOTIFICATION_CHANNEL";

    private LocationSource locationSource;

    @Inject PlaybackEngine playbackEngine;
    @Inject LocationManager locationManager;

    public static void start(Context context) {
        context.startService(new Intent(ACTION_START, null, context, PlaybackService.class));
    }

    public static void stop(Context context) {
        context.startService(new Intent(ACTION_STOP, null, context, PlaybackService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.getAppInstance().getAppComponent().inject(this);
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
        locationSource = new JsonLocationSource("gpsplayback", new File(Environment.getExternalStorageDirectory(), "track.json"));
        playbackEngine.startPlayback(locationSource, this::stopMe);
    }

    private void stop() {
        locationSource.stop();
        stopMe();
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
