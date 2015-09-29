package com.develop.rodia.loveotrading.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import static com.develop.rodia.loveotrading.data.ResultContract.ResultEntry;

public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(ResultEntry.CONTENT_URI, null, null);
        Cursor cursor = mContext.getContentResolver().query(ResultEntry.CONTENT_URI, null, null, null, null);

        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager packageManager = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(), ResultProvider.class.getName());

        try {
            ProviderInfo providerInfo = packageManager.getProviderInfo(componentName, 0);

            assertEquals(providerInfo.authority, ResultContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(ResultEntry.CONTENT_URI);

        assertEquals(ResultEntry.CONTENT_TYPE, type);

        int testTeam = 23;

        type = mContext.getContentResolver().getType(ResultEntry.buildResultTeam(testTeam));
        assertEquals(ResultEntry.CONTENT_TYPE, type);

        long testDate = 1419120000L; // December 21st, 2014
        type = mContext.getContentResolver().getType(ResultEntry.buildResultTeamWithDate(testTeam, testDate));
        assertEquals(ResultEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(ResultEntry.buildResultTeamWithStartDate(testTeam, testDate));
        assertEquals(ResultEntry.CONTENT_TYPE, type);
    }

    public void testBasicQuery() {
        // First insert
        ResultDbHelper dbHelper = new ResultDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createResultValues();
        long rowId = db.insert(ResultEntry.TABLE_NAME, null, testValues);

        assertTrue(rowId != -1);
        db.close();

        Cursor resultCursor = mContext.getContentResolver().query(ResultEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.validateCursor("testBasicQuery", resultCursor, testValues);
    }

    public void testUpdate() {
        ContentValues values = TestUtilities.createResultValues();

        Uri locationUri = mContext.getContentResolver().
                insert(ResultEntry.CONTENT_URI, values);
        long resultRowId = ContentUris.parseId(locationUri);

        assertTrue(resultRowId != -1);
        Log.d(LOG_TAG, "New row id: " + resultRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(ResultEntry._ID, resultRowId);
        updatedValues.put(ResultEntry.COLUMN_HOME_TEAM, "Atletico Madrid");

        Cursor resultCursor = mContext.getContentResolver().query(ResultEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        resultCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                ResultEntry.CONTENT_URI, updatedValues, ResultEntry._ID + "= ?",
                new String[] { Long.toString(resultRowId)});
        assertEquals(count, 1);

        tco.waitForNotificationOrFail();

        resultCursor.unregisterContentObserver(tco);
        resultCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                ResultEntry.CONTENT_URI,
                null,
                ResultEntry._ID + " = " + resultRowId,
                null,
                null
        );

        TestUtilities.validateCursor("testUpdateLocation.  Error validating result entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createResultValues();

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ResultEntry.CONTENT_URI, true, tco);
        Uri resultUri = mContext.getContentResolver().insert(ResultEntry.CONTENT_URI, testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long rowId = ContentUris.parseId(resultUri);

        assertTrue(rowId != -1);

        Cursor cursor = mContext.getContentResolver().query(ResultEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ResultEntry.", cursor, testValues);
    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        TestUtilities.TestContentObserver resultObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ResultEntry.CONTENT_URI, true, resultObserver);

        deleteAllRecordsFromProvider();

        resultObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(resultObserver);
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertResultValues() {
        long currentTestDate = TestUtilities.TEST_DATE;
        long teamId = TestUtilities.TEST_TEAM_ID;
        long millisecondsInADay = 1000 * 60 * 60 * 24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate *= millisecondsInADay) {
            ContentValues resultValues = new ContentValues();

            resultValues.put(ResultContract.ResultEntry.COLUMN_HOME_TEAM, "Barcelona");
            resultValues.put(ResultContract.ResultEntry.COLUMN_AWAY_TEAM, "Real Madrid");
            resultValues.put(ResultContract.ResultEntry.COLUMN_HOME_SCORE, 5);
            resultValues.put(ResultContract.ResultEntry.COLUMN_AWAY_SCORE, 0);
            resultValues.put(ResultContract.ResultEntry.COLUMN_MATCH_DATE, currentTestDate);
            resultValues.put(ResultContract.ResultEntry.COLUMN_TEAM_ID, teamId);

            returnContentValues[i] = resultValues;
        }

        return returnContentValues;
    }

    public void testBulkInsert() {
        ContentValues[] bulkValues = createBulkInsertResultValues();
        TestUtilities.TestContentObserver resultObserver = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(ResultEntry.CONTENT_URI, true, resultObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(ResultEntry.CONTENT_URI, bulkValues);

        resultObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(resultObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(ResultEntry.CONTENT_URI, null, null, null, ResultEntry.COLUMN_MATCH_DATE + " ASC");

        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCursor("testBulkInsert", cursor, bulkValues[i]);
        }

        cursor.close();
    }
}
