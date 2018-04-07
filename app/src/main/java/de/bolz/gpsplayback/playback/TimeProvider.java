package de.bolz.gpsplayback.playback;

import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;

import javax.inject.Inject;

public class TimeProvider {

    @Inject
    public TimeProvider() {}

    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public long elapsedRealtimeNanos() {
        return SystemClock.elapsedRealtimeNanos();
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
