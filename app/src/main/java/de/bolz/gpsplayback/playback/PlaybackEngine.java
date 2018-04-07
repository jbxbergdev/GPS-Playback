package de.bolz.gpsplayback.playback;

import android.location.Location;
import android.location.LocationManager;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.bolz.gpsplayback.AppExecutors;

@Singleton
public class PlaybackEngine {

    private final LocationManager locationManager;
    private final AppExecutors appExecutors;
    private final TimeProvider timeProvider;
    private final BlockingQueue<Location> locationQueue = new ArrayBlockingQueue<>(1000);
    private long firstLocationTimestamp = -1l;
    private long playbackStartedAt;
    private long lastPostTime;

    @Inject
    public PlaybackEngine(LocationManager locationManager, AppExecutors appExecutors, TimeProvider timeProvider) {
        this.locationManager = locationManager;
        this.appExecutors = appExecutors;
        this.timeProvider = timeProvider;
    }

    public void startPlayback(LocationSource locationSource, OnFinishListener onFinishListener) {
        appExecutors.generalIO().execute(() -> {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onNewLocation(Location location) {
                    handleLocation(location);
                }
                @Override
                public void onFinished() {
                    long delay = lastPostTime - timeProvider.elapsedRealtime() + 100;
                    appExecutors.schedule(onFinishListener::onFinished, appExecutors.playbackThread(), delay);
                }
            };
            try {
                locationSource.start(locationListener);
            } catch (IOException e) {
                e.printStackTrace();
                onFinishListener.onFinished();
            }
        });
    }

    private void handleLocation(Location location) {
        try {
            locationQueue.put(location);
            scheduleLocation(location);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void scheduleLocation(Location location) {
        long locationTime = location.getTime();
        long now = timeProvider.elapsedRealtime();
        if (firstLocationTimestamp == -1l) {
            firstLocationTimestamp = locationTime;
            playbackStartedAt = now;
        }
        long absoluteTimeToPostLocation = locationTime + (playbackStartedAt - firstLocationTimestamp);
        long postDelay = absoluteTimeToPostLocation - now;

        System.out.println("##### postDelay = " + postDelay + ", absolute post time = " + absoluteTimeToPostLocation);
        appExecutors.schedule(() -> {
            try {
                Location locationFromQueue = locationQueue.take();
                locationFromQueue.setTime(System.currentTimeMillis());
                System.out.println("##### posting " + locationFromQueue);
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, locationFromQueue);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }, appExecutors.playbackThread(), postDelay);
        lastPostTime = absoluteTimeToPostLocation;
    }

    public interface OnFinishListener {
        void onFinished();
    }
}
