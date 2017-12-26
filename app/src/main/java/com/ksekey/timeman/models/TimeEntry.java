package com.ksekey.timeman.models;

import android.content.Intent;

/**
 * Created by kk on 26/12/2017.
 */

public class TimeEntry {
    private String id;
    private String date;
    private int timeInMinutes;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTimeInMinutes() {
        return timeInMinutes;
    }

    public void setTimeInMinutes(int timeInMinutes) {
        this.timeInMinutes = timeInMinutes;
    }
}
