package com.garmin.mwfit;

/**
 * Created by Jimmy on 2017/2/20.
 */

public class FitLap {

    /**
     * <Lap StartTime="2017-02-19T09:06:47.000Z">
     <TotalTimeSeconds>1540.854</TotalTimeSeconds>
     <DistanceMeters>0.0</DistanceMeters>
     <Calories>361</Calories>
     <AverageHeartRateBpm><Value>145</Value></AverageHeartRateBpm>
     <MaximumHeartRateBpm><Value>174</Value></MaximumHeartRateBpm>
     <Cadence></Cadence>
     <TriggerMethod>MANUAL</TriggerMethod>
     */

    private String startTime;
    private String totalTimeSeconds;
    private String distanceMeters;
    private String calories;
    private String averageHeartRateBpm;
    private String maximumHeartRateBpm;
    private String intensity;
    private String cadence;
    private String triggerMethod;
    private FitTrack fitTrack;

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setTotalTimeSeconds(String totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public String getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setDistanceMeters(String distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public String getDistanceMeters() {
        return distanceMeters;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getCalories() {
        return calories;
    }

    public void setAverageHeartRateBpm(String averageHeartRateBpm) {
        this.averageHeartRateBpm = averageHeartRateBpm;
    }

    public String getAverageHeartRateBpm() {
        return averageHeartRateBpm;
    }

    public void setMaximumHeartRateBpm(String maximumHeartRateBpm) {
        this.maximumHeartRateBpm = maximumHeartRateBpm;
    }

    public String getMaximumHeartRateBpm() {
        return maximumHeartRateBpm;
    }

    public void setCadence(String cadence) {
        this.cadence = cadence;
    }

    public String getCadence() {
        return cadence;
    }

    public void setTriggerMethod(String triggerMethod) {
        this.triggerMethod = triggerMethod;
    }

    public String getTriggerMethod() {
        return triggerMethod;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setFitTrack(FitTrack fitTrack) {
        this.fitTrack = fitTrack;
    }

    public FitTrack getFitTrack() {
        return fitTrack;
    }
}
