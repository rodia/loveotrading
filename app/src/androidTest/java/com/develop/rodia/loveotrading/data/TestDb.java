package com.develop.rodia.loveotrading.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(ResultDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<>();

        tableNameHashSet.add(ResultContract.ResultEntry.TABLE_NAME);

        mContext.deleteDatabase(ResultDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new ResultDbHelper(this.mContext).getWritableDatabase();

        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly", c.moveToFirst());

        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        assertTrue("Error: Your database was created without the results entry", tableNameHashSet.isEmpty());

        c = db.rawQuery("PRAGMA table_info(" + ResultContract.ResultEntry.TABLE_NAME + ")", null);

        assertTrue("Error: This means that we were unable to querty the dataase for table information.", c.moveToFirst());

        final HashSet<String> resultColumnHashSet = new HashSet<>();

        resultColumnHashSet.add(ResultContract.ResultEntry._ID);
        resultColumnHashSet.add(ResultContract.ResultEntry.COLUMN_HOME_TEAM);
        resultColumnHashSet.add(ResultContract.ResultEntry.COLUMN_AWAY_TEAM);
        resultColumnHashSet.add(ResultContract.ResultEntry.COLUMN_HOME_SCORE);
        resultColumnHashSet.add(ResultContract.ResultEntry.COLUMN_AWAY_SCORE);
        resultColumnHashSet.add(ResultContract.ResultEntry.COLUMN_MATCH_DATE);
        resultColumnHashSet.add(ResultContract.ResultEntry.COLUMN_TEAM_ID);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);

            resultColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required result entry columns", resultColumnHashSet.isEmpty());
        db.close();
    }

    public void testResultTable() {
        ResultDbHelper dbHelper = new ResultDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues resultValues = TestUtilities.createResultValues();

        long resultRowId = db.insert(ResultContract.ResultEntry.TABLE_NAME, null, resultValues);

        assertTrue(resultRowId != -1);

        Cursor resultCursor = db.query(
                ResultContract.ResultEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        assertTrue("Error: No records found", resultCursor.moveToFirst());

        TestUtilities.validateCurrentRecord("testInsertReadDb resultEntry failed to validate", resultCursor, resultValues);

        assertFalse("Error: More than one record returned from the results query", resultCursor.moveToNext());

        resultCursor.close();
        dbHelper.close();
    }

}
