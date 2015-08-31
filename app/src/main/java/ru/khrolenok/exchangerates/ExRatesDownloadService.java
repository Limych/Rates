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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        Log.d(Settings.LOG_TAG, "Service started");

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
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet httpRequest = new HttpGet(params[0]);   // Settings.Rates.sourceUrl
            httpRequest.setHeader("Accept", "application/json");

            InputStream inputStream = null;
            String json = null;
            try {
                HttpResponse httpResponse = httpClient.execute(httpRequest);
                HttpEntity entity = httpResponse.getEntity();
                inputStream = entity.getContent();

                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                json = sb.toString();
            } catch (Exception e) {
                return e.toString();
            } finally {
                try{ if( inputStream != null ) inputStream.close(); } catch (Exception ignored) {}
            }

            if( !json.isEmpty() ) {
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
//            if(result != null) {
//                Toast.makeText(context, "Exchange rates download error: " + result,
//                        Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(context, "Exchange rates updated",
//                        Toast.LENGTH_SHORT).show();
//            }

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    ExRatesWidgetProvider.class));
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId,
                        ExRatesWidgetProvider.buildLayout(context, appWidgetManager, appWidgetId));
            }
        }
    }
}
