package com.ksekey.timeman.database;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ksekey.timeman.models.TimeEntry;

import java.sql.SQLException;

/**
 * Created by kk on 26/12/2017.
 * Вспомиогательный класс для работы с базой данных
 */

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME    = "ormlite.db";
    private static final int    DATABASE_VERSION = 1;

    private Dao<TimeEntry, Integer> mTimeEntryDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, TimeEntry.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, TimeEntry.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* TimeEntry */

    public Dao<TimeEntry, Integer> getTimeEntryDao() throws SQLException {
        if (mTimeEntryDao == null) {
            mTimeEntryDao = getDao(TimeEntry.class);
        }

        return mTimeEntryDao;
    }

    @Override
    public void close() {
        mTimeEntryDao = null;

        super.close();
    }
}