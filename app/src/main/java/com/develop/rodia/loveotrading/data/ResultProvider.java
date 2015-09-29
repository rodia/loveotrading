package com.develop.rodia.loveotrading.data;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;

public class ResultProvider extends ContentProvider {
    private static final UriMatcher uriMatcher = buildUriMatcher();

    static final int RESULT = 100;
    static final int RESULT_WITH_TEAM = 101;
    static final int RESULT_WITH_TEAM_AND_DATE = 102;

    /**
     * Construcción del UriMatcher, Este UriMatcher hara posible
     * que por cada URI que se pase, se devuelva un valor constante
     * que nos permita identificar luego en el sistema
    * */
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ResultContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ResultContract.PATH_RESULT, RESULT);
        matcher.addURI(authority, ResultContract.PATH_RESULT + "/#", RESULT_WITH_TEAM);
        matcher.addURI(authority, ResultContract.PATH_RESULT + "/#/#", RESULT_WITH_TEAM_AND_DATE);

        return matcher;
    }

    // Metodos utilitarios
    private ResultDbHelper dbHelper;

    // result.team_id = ?
    private static final String teamSelection = ResultContract.ResultEntry.TABLE_NAME + "." + ResultContract.ResultEntry.COLUMN_TEAM_ID + "= ? ";
    // result.team_id = ? AND match_date >= ?
    private static final String teamWithStartDateSelection =
            ResultContract.ResultEntry.TABLE_NAME + "." + ResultContract.ResultEntry.COLUMN_TEAM_ID + "= ? AND " +
            ResultContract.ResultEntry.TABLE_NAME + "." + ResultContract.ResultEntry.COLUMN_MATCH_DATE + " <= ? ";
    // result.team_id = ? AND match_date = ?
    private static final String teamWithDateSelection =
            ResultContract.ResultEntry.TABLE_NAME + "." + ResultContract.ResultEntry.COLUMN_TEAM_ID + "= ? AND " +
            ResultContract.ResultEntry.TABLE_NAME + "." + ResultContract.ResultEntry.COLUMN_MATCH_DATE + " = ? ";

    private Cursor getResultByTeam(Uri uri, String []projection, String sortOrder) {
        int teamSetting = ResultContract.ResultEntry.getTeamFromUri(uri);
        long startDate = ResultContract.ResultEntry.getStartDateFromUri(uri);

        String [] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = teamSelection;
            selectionArgs = new String [] {Integer.toString(teamSetting)};
        } else {
            selection = teamWithStartDateSelection;
            selectionArgs = new String [] {Integer.toString(teamSetting), Long.toString(startDate)};
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(ResultContract.ResultEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

    }

    private Cursor getResultByTeamAndDate(Uri uri, String []projection, String sortOrder) {
        int teamSetting = ResultContract.ResultEntry.getTeamFromUri(uri);
        long date = ResultContract.ResultEntry.getDateFromUri(uri);

        String [] selectionArgs;
        String selection;

        selection = teamWithDateSelection;
        selectionArgs = new String [] {Integer.toString(teamSetting), Long.toString(date)};

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(ResultContract.ResultEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(ResultContract.ResultEntry.COLUMN_MATCH_DATE)) {
            long dateValue = values.getAsLong(ResultContract.ResultEntry.COLUMN_MATCH_DATE);
            values.put(ResultContract.ResultEntry.COLUMN_MATCH_DATE, ResultContract.normalizeDate(dateValue));
        }
    }

    /**
     *  Metodos a sobreescribir!!
    * */
    @Override
    public boolean onCreate() {
        dbHelper = new ResultDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);

        switch (match) {
            case RESULT:
                return ResultContract.ResultEntry.CONTENT_TYPE;
            case RESULT_WITH_TEAM:
                return ResultContract.ResultEntry.CONTENT_TYPE;
            case RESULT_WITH_TEAM_AND_DATE:
                return ResultContract.ResultEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (uriMatcher.match(uri)) {
            case RESULT:
                retCursor = dbHelper.getReadableDatabase().query(ResultContract.ResultEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case RESULT_WITH_TEAM:
                retCursor = getResultByTeam(uri, projection, sortOrder);
                break;
            case RESULT_WITH_TEAM_AND_DATE:
                retCursor = getResultByTeamAndDate(uri, projection, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        Uri returnUri;

        if (match == RESULT) {
            normalizeDate(values);
            long id = db.insert(ResultContract.ResultEntry.TABLE_NAME, null, values);

            if (id > 0)
                returnUri = ResultContract.ResultEntry.buildResulUri(id);
            else
                throw new SQLException("Failed to insert row into " + uri);
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int deleted;

        if (selection == null)
            selection = "1";

        if (match == RESULT) {
            deleted = db.delete(ResultContract.ResultEntry.TABLE_NAME, selection, selectionArgs);
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int updated;

        if (selection == null)
            selection = "1";

        if (match == RESULT) {
            updated = db.update(ResultContract.ResultEntry.TABLE_NAME, values, selection, selectionArgs);
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return updated;
    }

    // Optional Methods!

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);

        if (match == RESULT) {
            db.beginTransaction();
            int count = 0;

            try {
                for (ContentValues value : values) {
                    normalizeDate(value);
                    long id = db.insert(ResultContract.ResultEntry.TABLE_NAME, null, value);

                    if (id != -1)
                        ++count;
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);

            return count;
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }
}
