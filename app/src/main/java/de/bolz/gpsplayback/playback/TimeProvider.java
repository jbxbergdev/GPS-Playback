package de.bolz.gpsplayback.playback;

import android.os.SystemClock;

import javax.inject.Inject;

public class TimeProvider {

    @Inject
    public TimeProvider() {}

    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }
}
