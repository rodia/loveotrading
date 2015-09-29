package com.develop.rodia.loveotrading.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.develop.rodia.loveotrading.data.ResultContract.ResultEntry;

public class ResultDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "result.db";

    public ResultDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + ResultEntry.TABLE_NAME + " (" +
                ResultEntry._ID + " INTEGER PRIMARY KEY," +
                ResultEntry.COLUMN_HOME_TEAM + " TEXT NOT NULL," +
                ResultEntry.COLUMN_AWAY_TEAM + " TEXT NOT NULL," +
                ResultEntry.COLUMN_HOME_SCORE + " INTEGER NOT_NULL," +
                ResultEntry.COLUMN_AWAY_SCORE + " INTEGER NOT_NULL," +
                ResultEntry.COLUMN_MATCH_DATE + " INTEGER NOT_NULL," +
                ResultEntry.COLUMN_TEAM_ID + " INTEGER NOT_NULL" +
                " );";
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ResultEntry.TABLE_NAME);

        onCreate(db);
    }
}
