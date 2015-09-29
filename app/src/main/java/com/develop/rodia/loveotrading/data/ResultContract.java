package com.develop.rodia.loveotrading.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

public class ResultContract {

    // El valor de Content Authority es un nombre para el content provider, esto es
    // simular a las relaciones entre el nombre de dominio de un sitio web.
    // Una cadena conveniente para el "Content Authority" es el nombre del paquete
    // de la aplicacion, que se garantiza que sea unico en el dispositivo
    public static final String CONTENT_AUTHORITY = "com.develop.rodia.loveotrading";

    // Esta es la URI Basica que identificara a nuestro Content Provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Paths posibles que se añaden a la URI base
    // Por ejemplo, la url content://com.develop.rodia.loveotrading/result
    // Obtendra una lista de todos los resultados, pero una url como
    // content://com.develop.rodia.loveotrading/loquesea devolvera un error
    public static final String PATH_RESULT = "result";

    public static long normalizeDate(long startDate) {
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);

        return time.setJulianDay(julianDay);
    }

    // Clase interna que define nuestro contrato con la columna Base
    public static final class ResultEntry implements BaseColumns {
        // Path base de results
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RESULT).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RESULT;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RESULT;

        public static final String TABLE_NAME = "result";

        public static final String COLUMN_HOME_TEAM = "home_team_name";
        public static final String COLUMN_AWAY_TEAM = "away_team_name";
        public static final String COLUMN_HOME_SCORE = "home_score";
        public static final String COLUMN_AWAY_SCORE = "away_score";
        public static final String COLUMN_MATCH_DATE = "match_date";
        public static final String COLUMN_TEAM_ID = "team_id";

        public static Uri buildResulUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildResultTeam(int team) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(team)).build();
        }

        public static Uri buildResultTeamWithStartDate(int team, long date) {
            long normalizeDate = normalizeDate(date);

            return CONTENT_URI.buildUpon().appendPath(Integer.toString(team))
                    .appendQueryParameter(COLUMN_MATCH_DATE, Long.toString(normalizeDate)).build();
        }

        public static Uri buildResultTeamWithDate(int team, long date) {
            long normalizeDate = normalizeDate(date);

            return CONTENT_URI.buildUpon().appendPath(Integer.toString(team))
                    .appendPath(Long.toString(normalizeDate)).build();
        }

        public static int getTeamFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_MATCH_DATE);
            if (dateString != null && dateString.length() > 0) {
                return Long.parseLong(dateString);
            } else
                return 0;
        }
    }
}
