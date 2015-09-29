package com.develop.rodia.loveotrading;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResultAdapter extends CursorAdapter {
    public ResultAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private String convertCursorToResult(Cursor cursor) {
        return String.format("%s: %d - %s: %d",
                cursor.getString(ResultsFragment.COL_HOME_TEAM),
                cursor.getInt(ResultsFragment.COL_HOME_SCORE),
                cursor.getString(ResultsFragment.COL_AWAY_TEAM),
                cursor.getInt(ResultsFragment.COL_AWAY_SCORE));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.result_view, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView)view;

        textView.setText(convertCursorToResult(cursor));
    }
}
