package de.bolz.gpsplayback.playback;

import java.io.IOException;

/**
 * Created by johannes on 15.03.18.
 */

public abstract class LocationSource {

    private final String providerName;

    LocationSource(String providerName) {
        this.providerName = providerName;
    }

    abstract void start(LocationListener locationListener) throws IOException;
    abstract void stop();

    public String getProviderName() {
        return providerName;
    }
}
