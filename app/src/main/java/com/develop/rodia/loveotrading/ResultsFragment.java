package com.develop.rodia.loveotrading;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import static com.develop.rodia.loveotrading.data.ResultContract.ResultEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class ResultsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ResultsFragment.class.getSimpleName();

    private static final int RESULT_LOADER = 0;

    private static final String[] RESULT_COLUMNS = {
            ResultEntry._ID,
            ResultEntry.COLUMN_HOME_TEAM,
            ResultEntry.COLUMN_AWAY_TEAM,
            ResultEntry.COLUMN_HOME_SCORE,
            ResultEntry.COLUMN_AWAY_SCORE,
            ResultEntry.COLUMN_MATCH_DATE
    };

    static final int COL_RESULT_ID = 0;
    static final int COL_HOME_TEAM = 1;
    static final int COL_AWAY_TEAM = 2;
    static final int COL_HOME_SCORE = 3;
    static final int COL_AWAY_SCORE = 4;
    static final int COL_MATCH_DATE = 5;

    private ResultAdapter resultAdapter;

    public ResultsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.result_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateResults();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        resultAdapter = new ResultAdapter(getActivity(), null, 0);

        ListView listView = (ListView)rootView.findViewById(R.id.result_list_view);

        listView.setAdapter(resultAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                int teamSetting = Utility.getPreferedTeam(getActivity());

                intent.setData(ResultEntry.buildResultTeamWithDate(teamSetting, cursor.getLong(COL_MATCH_DATE)));
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(RESULT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    private void updateResults() {
        FetchResultTask resultTask = new FetchResultTask(getActivity());
        int teamId = Utility.getPreferedTeam(getActivity());

        resultTask.execute(teamId);
    }

    public void onTeamChanged() {
        updateResults();
        getLoaderManager().restartLoader(RESULT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int teamSetting = Utility.getPreferedTeam(getActivity());
        int daysSetting = Utility.getPreferedDays(getActivity());

        long date = System.currentTimeMillis();

        String sortOrder = ResultEntry.COLUMN_MATCH_DATE + " DESC";
        Uri resultForTeamUri = ResultEntry.buildResultTeamWithStartDate(teamSetting, date);

        return new CursorLoader(getActivity(), resultForTeamUri, RESULT_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        resultAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        resultAdapter.swapCursor(null);
    }
}
