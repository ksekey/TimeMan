package com.ksekey.timeman.models;

import android.content.Intent;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by kk on 26/12/2017.
 */

@DatabaseTable(tableName = "timeEntry")
public class TimeEntry {

    @DatabaseField (id = true)
    private String id;

    @DatabaseField( dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
    private Date date;

    @DatabaseField
    private int timeInMinutes;

    @DatabaseField
    private String description;

    private User user;

    private Task task;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
