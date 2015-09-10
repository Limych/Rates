package ru.khrolenok.exchangerates;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.khrolenok.exchangerates.ui.WidgetProvider;

/**
 * Created by Limych on 08.07.2015.
 */
public class ExRatesDownloadService extends Service {

	public Context context = this;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(Settings.TAG, "Service started");

		final DownloadRatesTask task = new DownloadRatesTask();
		task.execute(Settings.Rates.sourceUrl);

		// I want to restart this service again
		AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarm.set(
				AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + Settings.Rates.reloadDelay,
				PendingIntent.getService(context, 0,
						new Intent(context, ExRatesDownloadService.class), 0)
		);

		// Here you can return one of some different constants.
		// This one in particular means that if for some reason
		// this service is killed, we don't want to start it
		// again automatically
		return START_NOT_STICKY;
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
				SharedPreferences.Editor prefs = context.getSharedPreferences(Settings.PREFS_NAME,
						Context.MODE_PRIVATE).edit();
				prefs.putString(Settings.Rates.ratesKey, json);
				prefs.apply();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
					WidgetProvider.class));
			for( int appWidgetId : appWidgetIds ) {
				appWidgetManager.updateAppWidget(appWidgetId,
						WidgetProvider.buildLayout(context, appWidgetManager, appWidgetId));
			}
		}
	}
}
