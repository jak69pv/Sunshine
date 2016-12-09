package com.alberto.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

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
                        R.id.list_item_forecast_textview,
                        new ArrayList<String>()
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
                updateWeather();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Funcion que se ejecuta en cuanto esta lista la actividad
    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private void updateWeather() {
        // Hilo que ejecutara en segundo plano el estado del tiempo actual
        Context context = getActivity().getApplicationContext();
        FetchWeatherTask weatherTask = new FetchWeatherTask(context, forecastAdapter);
        // Sacamos de las preferencias
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String location = prefs.getString(getString(R.string.pref_location_key), "46340,es");
        weatherTask.execute(location);
    }

}
