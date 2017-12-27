package com.ksekey.timeman.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by kk on 26/12/2017.
 */
@DatabaseTable(tableName = "user")
public class User {

    @DatabaseField(id = true)
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
