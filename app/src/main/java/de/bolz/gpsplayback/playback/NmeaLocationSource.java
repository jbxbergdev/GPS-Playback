package de.bolz.gpsplayback.playback;

import java.io.IOException;

public class NmeaLocationSource extends LocationSource{

    NmeaLocationSource(String providerName) {
        super(providerName);
    }

    @Override
    void start(LocationListener locationListener) throws IOException {
        // TODO
    }

    @Override
    void stop() {
        // TODO
    }
}
