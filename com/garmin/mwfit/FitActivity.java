package com.garmin.mwfit;

import java.util.ArrayList;

/**
 * Created by JImmy on 2017/2/20.
 */

public class FitActivity {

    private String sport;
    private ArrayList<FitLap> fitLaps = null;

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getSport() {
        return sport;
    }

    public ArrayList<FitLap> getFitLaps() {
        return fitLaps;
    }

    public void addLap(FitLap lap) {

        if (fitLaps == null) {

            fitLaps = new ArrayList<FitLap>();
        }
        fitLaps.add(lap);
    }
}
