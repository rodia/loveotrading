package com.develop.rodia.loveotrading.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {
    private static final int TEAM_ID = 38;
    private static final long TEST_DATE = 1419033600L;  // December 20th, 2014


    // content://com.develop.rodia.loveotrading/result
    private static Uri TEST_RESULT_DIR = ResultContract.ResultEntry.CONTENT_URI;
    private static Uri TEST_RESULT_WITH_TEAM_DIR = ResultContract.ResultEntry.buildResultTeam(TEAM_ID);
    private static Uri TEST_RESULT_WITH_TEAM_AND_DATE = ResultContract.ResultEntry.buildResultTeamWithDate(TEAM_ID, TEST_DATE);

    public void testUriMatcher() {
        UriMatcher testMatcher = ResultProvider.buildUriMatcher();

        assertEquals(testMatcher.match(TEST_RESULT_DIR), ResultProvider.RESULT);
        assertEquals(testMatcher.match(TEST_RESULT_WITH_TEAM_DIR), ResultProvider.RESULT_WITH_TEAM);
        assertEquals(testMatcher.match(TEST_RESULT_WITH_TEAM_AND_DATE), ResultProvider.RESULT_WITH_TEAM_AND_DATE);
    }

}
