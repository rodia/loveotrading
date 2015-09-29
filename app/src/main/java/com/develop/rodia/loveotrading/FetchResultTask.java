package com.develop.rodia.loveotrading;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;


class FetchResultTask extends AsyncTask<Integer, Void, Void> {
    private Context context;

    private static String LOG_TAG = FetchResultTask.class.getSimpleName();

    public FetchResultTask(Context theContext) {
        context = theContext;
    }


    @Override
    protected Void doInBackground(Integer... params) {
        if (params.length != 1)
            return null;

        String resultString = Utility.getJsonStringFromNetwork(Integer.toString(params[0]), "90");

        try {
            Utility.parseFixtureJson(resultString, params[0], context);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing" + e.getMessage(), e);
            e.printStackTrace();
            return null;
        }

        return null;
    }
}
