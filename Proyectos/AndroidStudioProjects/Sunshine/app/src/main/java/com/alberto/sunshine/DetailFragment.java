package com.alberto.sunshine;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alberto.sunshine.data.WeatherContract;
import com.alberto.sunshine.data.WeatherContract.WeatherEntry;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends android.app.Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static ShareActionProvider mShareActionProvider;
    private static String mForecast;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns loacation data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_PRESSURE = 6;
    static final int COL_WEATHER_WIND_SPEED = 7;
    static final int COL_WEATHER_DEGREES = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView =  (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        return new CursorLoader(
                getActivity(),
                intent.getData(),
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");

        if(!data.moveToFirst()) { return; }

        Context context = getActivity();

        // Read weather condition ID from cursor
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        // Use placeholder image
        mIconView.setImageResource(R.mipmap.ic_launcher);

        // Read date from cursor and update view for day of week and date
        long date = data.getLong(COL_WEATHER_DATE);
        String friendlyDateText = Utility.getFriendlyDayString(context, date);
        String dateText = Utility.getFormattedMonthDay(context, date);
        mFriendlyDateView.setText(friendlyDateText);
        mDateView.setText(dateText);

        // Read description from cursor and update view
        String description = data.getString(COL_WEATHER_DESC);
        mDescriptionView.setText(description);

        // Read high and low temperature from cursor and update view
        boolean isMetric = Utility.isMetric(getActivity());
        String highString = Utility.formatTemperature(context, data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String lowString = Utility.formatTemperature(context, data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        mHighTempView.setText(highString);
        mLowTempView.setText(lowString);

        // Read humidity from cursor and update view
        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        mHumidityView.setText(context.getString(R.string.format_humidity, humidity));

        // Read wind speed and direction from cursor and update view
        float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
        float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
        mWindView.setText(Utility.getFormattedWind(context, windSpeedStr, windDirStr));

        //Read pressure from cursor and update view
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        mPressureView.setText(context.getString(R.string.format_pressure, pressure));

        // We still need this for the share intent
        mForecast = String.format("%s - %s - %s/%s", dateText, description, highString, lowString);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static Intent createShareForecastIntent() {
        Intent shareIntent = new Intent()
                .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, mForecast)
                .setType("text/plain");

        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_share:
                mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

                if (mForecast != null) {
                    mShareActionProvider.setShareIntent(createShareForecastIntent());
                } else {
                    Log.d(LOG_TAG, "Share Action Provider is null");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}