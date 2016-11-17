package com.alberto.sunshine;

import android.app.Fragment;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.activity_main, new PlaceholderFragment())
                    .commit();
        }

    }

    // Este es el fragmento
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstance) {
            // Inflate the layout for this fragment
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            // Creamos el array de datos dummy
            String[] forecastArray = {"Today - Sunny - 88/63", "Tomorrow - Foggy - 70/46",
                    "Weds - Cloudy - 72/63", "Thurs - Rainy - 64/31", "Fri - Foggy - 70/46",
                    "Sat - Sunny - 76/68"};

            List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

            // Adaptador para introducir los datos del array en el layout
            // Le pasamos el contexto (en este caso la actividad padre del fragmento),
            //  el id del layout, el id del componente interno que vamos a rellenar
            // del layout y por ultimo le pasamos la lisda de datos
            ArrayAdapter<String> forecastAdapter =
                    new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.list_item_forecast,
                        R.id.list_item_forecast_textview,
                        weekForecast
                );
            // Buscamos la referencia al list view
            ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
            // Introducimos la lista en el listView
            forecastListView.setAdapter(forecastAdapter);

            return rootView;
        }
    }

}
