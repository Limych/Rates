package ru.khrolenok.exchangerates;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

//import com.google.android.gms.analytics.GoogleAnalytics;
//import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Implementation of App Widget functionality.
 */
public class ExRatesWidgetProvider extends AppWidgetProvider {
    protected static int sColorUp;
    protected static int sColorDown;
    protected static boolean sIsLongFormat;

//    public static GoogleAnalytics analytics;
//    public static Tracker tracker;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

//        analytics = GoogleAnalytics.getInstance(context);
//        analytics.setLocalDispatchPeriod(1800);

//        tracker = analytics.newTracker("UA-289367-9");
//        tracker.enableExceptionReporting(true);
//        tracker.enableAdvertisingIdCollection(true);
//        tracker.enableAutoActivityTracking(true);

        AlarmManager alarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarm.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10,
                PendingIntent.getService(context, 0,
                        new Intent(context, ExRatesDownloadService.class), 0)
        );

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(Settings.LOG_TAG, "ExRatesWidgetProvider.onUpdate(…)");

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Bundle options=appWidgetManager.getAppWidgetOptions(appWidgetId);

            onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                    options);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.v(Settings.LOG_TAG, "ExRatesWidgetProvider.onDeleted(…)");

        // When the user deletes the widget, delete the preference associated with it.
//        for (int appWidgetId : appWidgetIds) {
//            ExchangeWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
//        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        Log.v(Settings.LOG_TAG, "ExRatesWidgetProvider.onAppWidgetOptionsChanged(…, " + appWidgetId + ", …)");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId,
                buildLayout(context, appWidgetManager, appWidgetId));

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    protected static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    /**
     * Returns layout for widget.
     *
     * @param context Application context
     * @param appWidgetManager Widget manager
     * @param appWidgetId Widget ID
     * @return  Widget layout
     */
    protected static RemoteViews buildLayout(Context context, AppWidgetManager appWidgetManager,
                            int appWidgetId) {
        Log.v(Settings.LOG_TAG, "ExRatesWidgetProvider.buildLayout(…, " + appWidgetId + ")");

        final Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        final int widgetMinWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        final int widgetMinHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        final int wCells = getCellsForSize(widgetMinWidth);
        final int hCells = getCellsForSize(widgetMinHeight);
        final int widgetLayout = ( wCells > hCells ? R.layout.widget_layout_h
                : R.layout.widget_layout_v);
        final boolean isUseCompactView = ( widgetLayout == R.layout.widget_layout_h
                ? (wCells <= 3) : (wCells <= 1));
        final boolean isShowChange = ( widgetLayout == R.layout.widget_layout_v && wCells >= 2 );

        RemoteViews widget = new RemoteViews(context.getPackageName(), widgetLayout);
        widget.removeAllViews(R.id.rates);

        SharedPreferences prefs = context.getSharedPreferences(Settings.PREFS_NAME,
                Context.MODE_PRIVATE);
        sColorUp = prefs.getInt(Settings.Display.colorUp,     R.color.change_green);
        sColorDown = prefs.getInt(Settings.Display.colorDown, R.color.change_red);
//        sIsLongFormat = prefs.getBoolean(Settings.Display.rateFormat, false);
        sIsLongFormat = ( widgetLayout == R.layout.widget_layout_h && wCells >= 4 )
                || ( widgetLayout == R.layout.widget_layout_v && wCells >= 2 );

        List<String> ratesList;
        try {
            ratesList = Arrays.asList(prefs.getString(Settings.Display.ratesList,
                    Settings.Display.ratesListDefault).split("\\s*,\\s*"));
        } catch (NullPointerException e) {
            return widget;
        }

        JSONObject ratesJson;
        try {
            ratesJson = new JSONObject(prefs.getString(Settings.Rates.ratesKey, ""));
        } catch (JSONException e) {
            return widget;
        }

        Date updateTS = new Date(0);

        widget.addView(R.id.rates,
                buildRatesGroup(context, context.getString(R.string.title_official), ratesJson,
                        ratesList, Settings.Rates.Types.OFFICIAL, isUseCompactView, isShowChange,
                        updateTS));
        if ( wCells >= 2 || hCells >= 2 ) {
            widget.addView(R.id.rates,
                    buildRatesGroup(context, context.getString(R.string.title_stock), ratesJson,
                            ratesList, Settings.Rates.Types.STOCK, isUseCompactView, isShowChange,
                            updateTS));
        }
        if ( wCells >= 3 || hCells >= 2 ) {
            widget.addView(R.id.rates,
                    buildRatesGroup(context, context.getString(R.string.title_forex), ratesJson,
                            ratesList, Settings.Rates.Types.FOREX, isUseCompactView, isShowChange,
                            updateTS));
        }

        Log.d(Settings.LOG_TAG, "updateTS = " + updateTS);

        // Show updating timestamp
        final boolean isLongUpdateText = ( wCells >= 2 );
        final int updateTextPrefixId = ( isLongUpdateText
                ? R.string.text_updated
                : R.string.text_updated_short );
        final int updateTextFormatFlags = ( isLongUpdateText
                ? android.text.format.DateUtils.FORMAT_SHOW_DATE
                    | android.text.format.DateUtils.FORMAT_SHOW_TIME
                    | android.text.format.DateUtils.FORMAT_ABBREV_ALL
                : android.text.format.DateUtils.FORMAT_SHOW_TIME
                    | android.text.format.DateUtils.FORMAT_ABBREV_ALL );
        final String updateText = context.getString(updateTextPrefixId)
                + " " + android.text.format.DateUtils.formatDateTime(context, updateTS.getTime(),
                    updateTextFormatFlags);
        widget.setTextViewText(R.id.update_timestamp, updateText);

        return widget;
    }

    protected static RemoteViews buildRatesGroup(Context context, String caption,
                     JSONObject ratesJson, List<String> ratesList, String ratesType,
                     boolean isUseCompactView, boolean isShowChange, Date updateTS) {
        if( isUseCompactView ){
            isShowChange = false;
        }

        ExRatesGroup exRatesGroup = new ExRatesGroup(caption, ratesType, ratesList, ratesJson);

        exRatesGroup.isCompactTitleMode = isUseCompactView;
        exRatesGroup.isShortFormat = !sIsLongFormat;

        exRatesGroup.calcViewsBounds(context);

        final Date groupTS = exRatesGroup.getUpdateTS();
        if( groupTS.after(updateTS) ){
            updateTS.setTime(groupTS.getTime());
        }

        return exRatesGroup.buildRemoteViews(context, isShowChange, false);
    }
}

