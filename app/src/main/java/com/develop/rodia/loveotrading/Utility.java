package com.develop.rodia.loveotrading;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.develop.rodia.loveotrading.data.ResultContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Formatter;

import static com.develop.rodia.loveotrading.data.ResultContract.ResultEntry;

/**
 * Created by Pablo on 3/10/2015.
 */
public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static String getJsonStringFromNetwork(String team, String days) {
        Log.d(LOG_TAG, "Starting network connection");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String timeFrame = "p" + days;

        try {
            final String FIXTURE_BASE_URL = "http://restle.hostingla.in/trades/api/";
            final String FIXTURE_PATH = "fixtures";
            final String TIME_FRAME_PARAMETER = "timeFrame";

            Uri builtUri = Uri.parse(FIXTURE_BASE_URL).buildUpon()
                    .appendPath(team)
                    .appendPath(FIXTURE_PATH)
                    .appendQueryParameter(TIME_FRAME_PARAMETER, timeFrame)
                    .build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null)
                return "";
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0)
                return "";

            return buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

    public static void parseFixtureJson(String fixtureJson, int teamID, Context context) throws JSONException {
        JSONObject jsonObject = new JSONObject(fixtureJson);
        ArrayList<ContentValues> values = new ArrayList<>();

        final String LIST = "fixtures";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_SCORE = "goalsHomeTeam";
        final String AWAY_SCORE = "goalsAwayTeam";
        final String DATE = "date";
        Time dayTime = new Time();
        dayTime.setToNow();

        int julianStartDate = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        JSONArray fixturesArray = jsonObject.getJSONArray(LIST);

        for (int i = 0; i < fixturesArray.length(); i++) {
            String homeTeam;
            String awayTeam;
            int homeScore;
            int awayScore;
            long date;
            JSONObject matchObject = fixturesArray.getJSONObject(i);
            JSONObject resultObject = matchObject.getJSONObject(RESULT);

            homeTeam = matchObject.getString(HOME_TEAM);
            awayTeam = matchObject.getString(AWAY_TEAM);
            homeScore = resultObject.getInt(HOME_SCORE);
            awayScore = resultObject.getInt(AWAY_SCORE);
            date = dayTime.setJulianDay(julianStartDate - i);

            ContentValues content = new ContentValues();

            content.put(ResultEntry.COLUMN_HOME_TEAM, homeTeam);
            content.put(ResultEntry.COLUMN_AWAY_TEAM, awayTeam);
            content.put(ResultEntry.COLUMN_HOME_SCORE, homeScore);
            content.put(ResultEntry.COLUMN_AWAY_SCORE, awayScore);
            content.put(ResultEntry.COLUMN_MATCH_DATE, date);
            content.put(ResultEntry.COLUMN_TEAM_ID, teamID);

            values.add(content);
        }

        int inserted = 0;

        if (values.size() > 0) {
            ContentValues[] valuesArray = new ContentValues[values.size()];

            values.toArray(valuesArray);
            inserted = context.getContentResolver().bulkInsert(ResultEntry.CONTENT_URI, valuesArray);
        }

        Log.d(LOG_TAG, "FetchResult Complete " + inserted + " inserted");
    }

    public static int getPreferedTeam(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_team_key), context.getString(R.string.pref_barcelona_key)));
    }

    public static int getPreferedDays(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_days_key), context.getString(R.string.pref_days_default)));
    }
}
