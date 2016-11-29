package com.alberto.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

public class ForecastFragment extends android.app.Fragment {

    //Lista donde va la info a poner
    private ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Añadimos esta linea para indicar que tiene eventos de menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstance) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Adaptador para introducir los datos del array en el layout
        // Le pasamos el contexto (en este caso la actividad padre del fragmento),
        //  el id del layout, el id del componente interno que vamos a rellenar
        // del layout y por ultimo le pasamos la lisda de datos
        forecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.list_item_forecast,
                        R.id.list_item_forecast_textview
                );
        // Buscamos la referencia al list view
        final ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        // Introducimos la lista en el listView
        forecastListView.setAdapter(forecastAdapter);
        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // En los fragmentos no existe la funcion getApplicationContext, asique tenemos que
                // pedir primero la actividad
                Context context = getActivity().getApplicationContext();
                String forecast = forecastAdapter.getItem(position);
                Intent detailIntent = new Intent(context, DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailIntent);
            }
        });

        // Hilo que ejecutara en segundo plano el estado del tiempo actual
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        // Sacamos de las preferencias
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String location = prefs.getString(getString(R.string.pref_location_key), "46340,es");
        weatherTask.execute(location);

        return rootView;
    }

    // Insertamos el menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // Hilo que ejecutara en segundo plano el estado del tiempo actual
                FetchWeatherTask weatherTask = new FetchWeatherTask();
                // Sacamos de las preferencias
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                String location = prefs.getString(getString(R.string.pref_location_key), "46340,es");
                weatherTask.execute(location);
                return true;
            //case R.id.action_settings:
            //    Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            //    startActivity(settingsIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class URLParameters {
        final String SCHEME = "http";
        final String AUTHORITY = "api.openweathermap.org";
        final String DATA = "data";
        final String VERSION = "2.5";
        final String FORECAST = "forecast";
        final String DAILY = "daily";
        final String POSTAL_CODE = "q";
        //final JSONObject mode = new JSONObject("{type : mode, data : json}");
        public String[] mode() { return new String[] {"mode","json"}; }
        public String[] units() { return new String[] {"units","metrics"}; }
        public String[] count() { return new String[] {"cnt","7"}; }
        final String APPID = "APPID";
    }

    private class FetchWeatherTask extends AsyncTask<String,Void,String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /* MEtodos AUXILIARES JSON */

        /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String postalCode = params[0];
            URLParameters parameters = new URLParameters();

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.scheme(parameters.SCHEME)
                        .authority(parameters.AUTHORITY)
                        .appendPath(parameters.DATA)
                        .appendPath(parameters.VERSION)
                        .appendPath(parameters.FORECAST)
                        .appendPath(parameters.DAILY)
                        .appendQueryParameter(parameters.POSTAL_CODE,postalCode)
                        .appendQueryParameter(parameters.mode()[0],parameters.mode()[1])
                        .appendQueryParameter(parameters.units()[0],parameters.units()[1])
                        .appendQueryParameter(parameters.count()[0],parameters.count()[1])
                        .appendQueryParameter(parameters.APPID,BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

                // Creamos la url
                URL url = new URL(uriBuilder.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, Integer.parseInt(parameters.count()[1]));
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // Esto ocurrira tan solo cuando haya un error obteniend o parseando el json
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            if(strings != null) {
                forecastAdapter.clear();
                for (String dayForecastStr : strings) {
                   forecastAdapter.add(dayForecastStr);
                }
            }

        }

    }
}
