package de.bolz.gpsplayback;

import java.io.IOException;

/**
 * Created by johannes on 15.03.18.
 */

public abstract class LocationSource {

    private final String providerName;
    private final LocationListener locationListener;

    LocationSource(String providerName, LocationListener locationListener) {
        this.providerName = providerName;
        this.locationListener = locationListener;
    }

    abstract void start() throws IOException;
    abstract void stop();

    public String getProviderName() {
        return providerName;
    }

    public LocationListener getLocationListener() {
        return locationListener;
    }
}
