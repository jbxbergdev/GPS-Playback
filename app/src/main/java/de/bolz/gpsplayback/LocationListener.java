package de.bolz.gpsplayback;

import android.location.Location;

/**
 * Created by johannes on 15.03.18.
 */

public interface LocationListener {
    void onNewLocation(Location location);
    void onFinished();
}
