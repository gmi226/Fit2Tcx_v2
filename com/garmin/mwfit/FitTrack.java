package com.garmin.mwfit;

import java.util.ArrayList;

/**
 * Created by Jimmy on 2017/2/20.
 */

public class FitTrack {

    private ArrayList<FitTrackPoint> fitTrackPoints = null;

    public ArrayList<FitTrackPoint> getFitTrackPoints() {
        return fitTrackPoints;
    }

    public void addFitTrackPoint(FitTrackPoint point) {

        if (fitTrackPoints == null) {

            fitTrackPoints = new ArrayList<FitTrackPoint>();
        }
        fitTrackPoints.add(point);
    }
}
