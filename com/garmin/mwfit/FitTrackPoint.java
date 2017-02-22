package com.garmin.mwfit;

/**
 * Created by Jimmy on 2017/2/20.
 */

public class FitTrackPoint {

    private String time;
    private String latitudeDegrees;
    private String longitudeDegrees;
    private String altitudeMeters;
    private String distanceMeters;
    private String heartRateBpm;
    private String cadence;
    private String speed;

    public void setLatitudeDegrees(String latitudeDegrees) {
        this.latitudeDegrees = latitudeDegrees;
    }

    public String getLatitudeDegrees() {
        return latitudeDegrees;
    }

    public void setLongitudeDegrees(String longitudeDegrees) {
        this.longitudeDegrees = longitudeDegrees;
    }

    public String getLongitudeDegrees() {
        return longitudeDegrees;
    }

    public void setAltitudeMeters(String altitudeMeters) {
        this.altitudeMeters = altitudeMeters;
    }

    public String getAltitudeMeters() {
        return altitudeMeters;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setDistanceMeters(String distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public String getDistanceMeters() {
        return distanceMeters;
    }

    public void setHeartRateBpm(String heartRateBpm) {
        this.heartRateBpm = heartRateBpm;
    }

    public String getHeartRateBpm() {
        return heartRateBpm;
    }

    public void setCadence(String cadence) {
        this.cadence = cadence;
    }

    public String getCadence() {
        return cadence;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getSpeed() {
        return speed;
    }
}
