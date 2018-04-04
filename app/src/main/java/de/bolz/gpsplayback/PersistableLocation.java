package de.bolz.gpsplayback;

import android.location.Location;
import android.os.Build;

/**
 * Created by johannes on 15.03.18.
 */

public class PersistableLocation {

    public long time = 0l;
    public long elapsedRealtimeNanos = 0l;
    public double latitude = 0.d;
    public double longitude = 0.d;
    public Double altitude;
    public Float speed;
    public Float bearing;
    public Float horizontalAccuracyMeters;
    public Float verticalAccuracyMeters;
    public Float speedAccuracyMetersPerSecond;
    public Float bearingAccuracyDegrees;

    public static Location toLocation(PersistableLocation persistableLocation, String provider) {
        Location location = new Location(provider);
        location.setTime(persistableLocation.time);
        location.setLatitude(persistableLocation.latitude);
        location.setLongitude(persistableLocation.longitude);

        if (persistableLocation.altitude != null) {
            location.setAltitude(persistableLocation.altitude);
        }
        if (persistableLocation.speed != null) {
            location.setSpeed(persistableLocation.speed);
        }
        if (persistableLocation.bearing != null) {
            location.setBearing(persistableLocation.bearing);
        }
        if (persistableLocation.horizontalAccuracyMeters != null) {
            location.setAccuracy(persistableLocation.horizontalAccuracyMeters);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(persistableLocation.elapsedRealtimeNanos);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (persistableLocation.verticalAccuracyMeters != null) {
                location.setVerticalAccuracyMeters(persistableLocation.verticalAccuracyMeters);
            }
            if (persistableLocation.speedAccuracyMetersPerSecond != null) {
                location.setSpeedAccuracyMetersPerSecond(persistableLocation.speedAccuracyMetersPerSecond);
            }
            if (persistableLocation.bearingAccuracyDegrees != null) {
                location.setBearingAccuracyDegrees(persistableLocation.bearingAccuracyDegrees);
            }
        }
        return location;
    }

    public static PersistableLocation fromLocation(Location location) {
        PersistableLocation persistableLocation = new PersistableLocation();
        persistableLocation.time = location.getTime();
        persistableLocation.latitude = location.getLatitude();
        persistableLocation.longitude = location.getLongitude();

        if (location.hasAltitude()) {
            persistableLocation.altitude = location.getAltitude();
        }
        if (location.hasSpeed()) {
            persistableLocation.speed = location.getSpeed();
        }
        if (location.hasBearing()) {
            persistableLocation.bearing = location.getBearing();
        }
        if (location.hasAccuracy()) {
            persistableLocation.horizontalAccuracyMeters = location.getAccuracy();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            persistableLocation.elapsedRealtimeNanos = location.getElapsedRealtimeNanos();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (location.hasBearingAccuracy()) {
                persistableLocation.bearingAccuracyDegrees = location.getBearingAccuracyDegrees();
            }
            if (location.hasVerticalAccuracy()) {
                persistableLocation.verticalAccuracyMeters = location.getVerticalAccuracyMeters();
            }
            if (location.hasSpeedAccuracy()) {
                persistableLocation.speedAccuracyMetersPerSecond = location.getSpeedAccuracyMetersPerSecond();
            }
        }
        return persistableLocation;
    }
}
