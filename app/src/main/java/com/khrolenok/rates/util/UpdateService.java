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

package com.khrolenok.rates.util;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.khrolenok.rates.BuildConfig;
import com.khrolenok.rates.Settings;
import com.khrolenok.rates.ui.WidgetProvider;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import trikita.log.Log;

/**
 * Created by Limych on 08.07.2015
 */
public class UpdateService extends IntentService {

	protected static long restartTime;

	public UpdateService() {
		super(UpdateService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if( System.currentTimeMillis() < restartTime ) return;

		if( BuildConfig.DEBUG ) Log.v("Update service started");

		if( !ConnectionMonitor.isNetworkAvailable(getBaseContext()) ){
			if( BuildConfig.DEBUG ) Log.v("Update suspended until network available");

			ConnectionMonitor.setEnabledSetting(this, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

			return;
		}

		ConnectionMonitor.setEnabledSetting(this, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

		downloadUpdate();

		// I want to restart this service again
		scheduleRestart(this, Settings.Rates.reloadDelay);
	}

	public static void start(Context context) {
		context.startService(new Intent(context, UpdateService.class));
	}

	public static void scheduleRestart(Context context, int timeLag) {
		restartTime = System.currentTimeMillis() + timeLag;
		final AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		alarm.set(
				AlarmManager.RTC_WAKEUP,
				restartTime,
				PendingIntent.getService(context, 0, new Intent(context, UpdateService.class), 0)
		);
	}

	private void downloadUpdate() {
		InputStream inputStream = null;
		String json = null;
		try{
			URL url = new URL(Settings.Rates.sourceUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Accept", "application/json");
			try{
				inputStream = new BufferedInputStream(urlConnection.getInputStream());

				// json is UTF-8 by default
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line;
				while( ( line = reader.readLine() ) != null ){
					sb.append(line).append("\n");
				}
				json = sb.toString();
			} finally {
				urlConnection.disconnect();
			}

		} catch( Exception e ){
			if( BuildConfig.DEBUG ) Log.d(e.getStackTrace());

		} finally {
			try{
				if( inputStream != null ) inputStream.close();
			} catch( Exception ignored ){
			}
		}

		if( json != null && !json.isEmpty() ){
			if( BuildConfig.DEBUG ) Log.v("Update loaded");

			SharedPreferences.Editor prefs = getApplicationContext()
					.getSharedPreferences(Settings.PREFS_NAME, Context.MODE_PRIVATE).edit();
			prefs.putString(Settings.Rates.ratesKey, json);
			prefs.apply();

			WidgetProvider.notifyUpdateNeeded(this);
			// TODO: 14.09.2015 Make activity direct updating
		}
	}
}