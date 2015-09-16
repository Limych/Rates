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
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.khrolenok.rates.BuildConfig;
import com.khrolenok.rates.Settings;
import com.khrolenok.rates.ui.WidgetProvider;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Limych on 08.07.2015
 */
public class RatesDownloadService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if( BuildConfig.DEBUG ) Log.d(Settings.TAG, "Rates download service started");

		final DownloadRatesTask task = new DownloadRatesTask();
		task.execute(Settings.Rates.sourceUrl);

		// I don't want this service to stay in memory, so I stop it
		// immediately after doing what I wanted it to do.
		stopSelf();

		// Here you can return one of some different constants.
		// This one in particular means that if for some reason
		// this service is killed, we don't want to start it
		// again automatically
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// I want to restart this service again
		scheduleRestart(this, Settings.Rates.reloadDelay);
	}

	public static void scheduleRestart(Context context, int timeLag){
		AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		alarm.set(
				AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + timeLag,
				PendingIntent.getService(context, 0, new Intent(context, RatesDownloadService.class), 0)
		);
	}

	class DownloadRatesTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			InputStream inputStream = null;
			String json = null;
			try{
				URL url = new URL(params[0]);
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
				return e.toString();
			} finally {
				try{
					if( inputStream != null ) inputStream.close();
				} catch( Exception ignored ){
				}
			}

			if( !json.isEmpty() ){
				SharedPreferences.Editor prefs = ((Context) RatesDownloadService.this ).getSharedPreferences(Settings.PREFS_NAME,
						Context.MODE_PRIVATE).edit();
				prefs.putString(Settings.Rates.ratesKey, json);
				prefs.apply();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			WidgetProvider.notifyUpdateNeeded((Context) RatesDownloadService.this);

			// TODO: 14.09.2015 Make activity direct updating
		}
	}
}
