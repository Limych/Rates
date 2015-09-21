/*
 * Copyright (c) 2015 Andrey “Limych” Khrolenok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.khrolenok.rates.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.khrolenok.rates.ExRatesApplication;
import com.khrolenok.rates.R;
import com.khrolenok.rates.util.UpdateService;

/**
 * Created by Limych on 03.09.2015
 */
public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		((TabLayout) findViewById(R.id.tabLayout)).setVisibility(View.GONE);

		if( savedInstanceState == null ){
			getFragmentManager().beginTransaction()
					.add(R.id.contentFrame, new SettingsFragment())
					.commit();
		}
	}

	public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);

			if( !ExRatesApplication.isTestDevice ){
				getPreferenceScreen().removePreference(findPreference("category_debug"));

			} else {
				final Preference deviceIdPref = findPreference("device_id");
				deviceIdPref.setSummary(ExRatesApplication.deviceId);
			}
		}

		@Override
		public void onResume() {
			super.onResume();

			// Set up a listener whenever a key changes
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			super.onPause();

			// Unregister the listener whenever a key changes
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			final Context context = getActivity();

			WidgetProvider.notifyUpdateNeeded(context);

			// Try to restart update service
			UpdateService.start(context);
		}
	}

}
