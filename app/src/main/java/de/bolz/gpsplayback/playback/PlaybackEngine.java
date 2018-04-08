package de.bolz.gpsplayback.playback;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.bolz.gpsplayback.AppExecutors;
import de.bolz.gpsplayback.Resource;

@Singleton
public class PlaybackEngine {

    private final LocationManager locationManager;
    private final AppExecutors appExecutors;
    private final TimeProvider timeProvider;
    private final BlockingQueue<Location> locationQueue = new ArrayBlockingQueue<>(1000);
    private long firstLocationTimestamp = -1l;
    private long playbackStartedAt;
    private long lastPostTime;
    private volatile boolean running = false;

    private LocationSource locationSource;
    private MutableLiveData<Resource<PlaybackState, Exception>> playbackStateLiveData = new MutableLiveData<>();

    @Inject
    public PlaybackEngine(LocationManager locationManager, AppExecutors appExecutors, TimeProvider timeProvider) {
        this.locationManager = locationManager;
        this.appExecutors = appExecutors;
        this.timeProvider = timeProvider;
    }

    public synchronized void startPlayback(LocationSource locationSource) {
        if (running) {
            throw new IllegalStateException("Playback already started");
        }
        running = true;
        this.locationSource = locationSource;
        appExecutors.diskIO().execute(() -> {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onNewLocation(Location location) {
                    handleLocation(location);
                }
                @Override
                public void onFinished() {
                    long delay = lastPostTime - timeProvider.elapsedRealtime() + 100;
                    appExecutors.schedule(() -> playbackStateLiveData.postValue(Resource.success(PlaybackState.STOPPED)), appExecutors.playbackThread(), delay);
                    running = false;
                }
            };
            try {
                locationSource.start(locationListener);
            } catch (IOException e) {
                playbackStateLiveData.postValue(Resource.error(PlaybackState.STOPPED, e));
                running = false;
            }
        });
    }

    public LiveData<Resource<PlaybackState, Exception>> getPlaybackState() {
        return playbackStateLiveData;
    }

    public synchronized void stopPlayback() {
        if (locationSource != null && running) {
            locationSource.stop();
            locationQueue.clear();
            locationSource = null;
        }
    }

    private void handleLocation(Location location) {
        if (!running) {
            return;
        }
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

        appExecutors.schedule(() -> {
            if (!running) {
                return;
            }
            try {
                Location locationFromQueue = locationQueue.take();
                locationFromQueue.setTime(timeProvider.currentTimeMillis());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    locationFromQueue.setElapsedRealtimeNanos(timeProvider.elapsedRealtimeNanos());
                }
                System.out.println("##### posting " + locationFromQueue);
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, locationFromQueue);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }, appExecutors.playbackThread(), postDelay);
        lastPostTime = absoluteTimeToPostLocation;
    }

    public enum PlaybackState {
        RUNNING, STOPPED
    }
}
