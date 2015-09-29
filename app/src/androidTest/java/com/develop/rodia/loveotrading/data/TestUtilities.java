package com.develop.rodia.loveotrading.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.develop.rodia.loveotrading.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {
    static final int TEST_TEAM_ID = 38;
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());

    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columName);
            assertFalse("Column '" + columName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString()
                            + "' did not match the expected value '" + expectedValue + "'. " + error,
                    expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createResultValues() {
        ContentValues resultValues = new ContentValues();

        resultValues.put(ResultContract.ResultEntry.COLUMN_HOME_TEAM, "Barcelona");
        resultValues.put(ResultContract.ResultEntry.COLUMN_AWAY_TEAM, "Real Madrid");
        resultValues.put(ResultContract.ResultEntry.COLUMN_HOME_SCORE, 5);
        resultValues.put(ResultContract.ResultEntry.COLUMN_AWAY_SCORE, 0);
        resultValues.put(ResultContract.ResultEntry.COLUMN_MATCH_DATE, TEST_DATE);
        resultValues.put(ResultContract.ResultEntry.COLUMN_TEAM_ID, TEST_TEAM_ID);

        return resultValues;
    }

    static long insertNewTeam(Context context) {
        ResultDbHelper dbHelper = new ResultDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createResultValues();

        long resultRowId = db.insert(ResultContract.ResultEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: Failure to insert test Result Values", resultRowId != -1);

        return resultRowId;
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
