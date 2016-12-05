/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alberto.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.support.v4.view.MenuItemCompat.getActionProvider;

public class DetailActivity extends AppCompatActivity {


    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.activity_detail, new DetailFragment())
                    .commit();
        }
        // Añade el botón de navegacion hacia atras. Por defecto es true
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                 return true;
             case R.id.action_share:
                ShareActionProvider mShareActionProvider = (ShareActionProvider) getActionProvider(item);
                TextView tw = (TextView) findViewById(R.id.detail_text);
                String textToSend = tw.getText() + FORECAST_SHARE_HASHTAG;

                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareForecastIntent(textToSend));
                } else {
                    Log.d(LOG_TAG, "Share Action Provider is null");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    private Intent createShareForecastIntent(String textToShare) {
        Intent shareIntent = new Intent()
                .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, textToShare)
                .setType("text/plain");

        return shareIntent;
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends android.app.Fragment {


        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                TextView textView = (TextView) rootView.findViewById(R.id.detail_text);
                //textView.setTextSize(40);
                textView.setText(forecastStr);
            }
            return rootView;
        }

    }
}