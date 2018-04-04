package de.bolz.gpsplayback;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Created by johannes on 15.03.18.
 */

public class JsonLocationSource extends LocationSource {

    private final File jsonFile;
    private volatile boolean isStarted = false;

    JsonLocationSource(String providerName, LocationListener locationListener, File jsonFile) {
        super(providerName, locationListener);
        this.jsonFile = jsonFile;
    }

    @Override
    void start() throws IOException {
        JsonParser jsonParser = null;
        isStarted = true;
        try {
            JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
            jsonParser = jsonFactory.createParser(jsonFile);
            if(jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected an array");
            }
            while (isStarted && jsonParser.nextToken() == JsonToken.START_OBJECT) {
                PersistableLocation persistableLocation = jsonParser.readValueAs(PersistableLocation.class);
                getLocationListener().onNewLocation(PersistableLocation.toLocation(persistableLocation, getProviderName()));
            }
            getLocationListener().onFinished();
        } finally {
            if (jsonParser != null) {
                try {
                    jsonParser.close();
                } catch (IOException ignored) {}
            }
            isStarted = false;
        }

    }

    @Override
    public void stop() {
        isStarted = false;
    }
}
