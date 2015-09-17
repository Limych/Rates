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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.khrolenok.rates.ExRatesGroup;
import com.khrolenok.rates.R;
import com.khrolenok.rates.Settings;
import com.khrolenok.rates.util.RatesDownloadService;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider extends AppWidgetProvider {
	protected static boolean sIsLongFormat;

	public static void notifyUpdateNeeded(Context context) {
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		final int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

		appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, android.R.id.list);

		final Intent update = new Intent(context.getApplicationContext(), WidgetProvider.class);
		update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		context.sendBroadcast(update);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		// Schedule to start rates update service
		RatesDownloadService.scheduleRestart(context, 10);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		for( int appWidgetId : appWidgetIds ) {
			final Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

			onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, options);
		}
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
	                                      int appWidgetId, Bundle newOptions) {
		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId,
				buildLayout(context, appWidgetManager, appWidgetId, newOptions));

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
		while( 70 * n - 30 < size ){
			++n;
		}
		return n - 1;
	}

	/**
	 * Returns layout for widget.
	 *
	 * @param context          Application mContext
	 * @param appWidgetManager Widget manager
	 * @param appWidgetId      Widget ID
	 * @param options          Widget options
	 * @return Widget layout
	 */
	public static RemoteViews buildLayout(Context context, AppWidgetManager appWidgetManager,
	                                      int appWidgetId, Bundle options) {
		final int widgetMinWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
		final int widgetMinHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
		final int wCells = getCellsForSize(widgetMinWidth);
		final int hCells = getCellsForSize(widgetMinHeight);
		final int widgetLayout = ( wCells > hCells ? R.layout.widget_layout_h
				: R.layout.widget_layout_v );
		final boolean isUseCompactView = ( widgetLayout == R.layout.widget_layout_h
				? ( wCells <= 3 ) : ( wCells <= 1 ) );
		final boolean isShowChange = ( widgetLayout == R.layout.widget_layout_v && wCells >= 2 );

		sIsLongFormat = PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(Settings.Preferences.LONG_FORMAT, false)
				&& ( widgetLayout == R.layout.widget_layout_h && wCells >= 4
				|| widgetLayout == R.layout.widget_layout_v && wCells >= 2 );

		final SharedPreferences dataCache = context.getSharedPreferences(Settings.PREFS_NAME,
				Context.MODE_PRIVATE);

		List<String> ratesList;
		try{
			ratesList = Arrays.asList(dataCache.getString(Settings.Display.ratesList,
					Settings.Display.ratesListDefault).split("\\s*,\\s*"));
		} catch( Exception ignored ){
			return new RemoteViews(context.getPackageName(), R.layout.widget_layout_loading);
		}

		JSONObject ratesJson;
		try{
			ratesJson = new JSONObject(dataCache.getString(Settings.Rates.ratesKey, null));
		} catch( Exception ignored ){
			return new RemoteViews(context.getPackageName(), R.layout.widget_layout_loading);
		}

		RemoteViews widget = new RemoteViews(context.getPackageName(), widgetLayout);
		widget.removeAllViews(R.id.rates);

		Date updateTS = new Date(0);

		widget.addView(R.id.rates,
				buildRatesGroup(context, context.getString(R.string.title_official) + ":", ratesJson,
						ratesList, Settings.Rates.Groups.OFFICIAL, isUseCompactView, isShowChange,
						updateTS));
		if( wCells >= 2 || hCells >= 2 ){
			widget.addView(R.id.rates,
					buildRatesGroup(context, context.getString(R.string.title_stock) + ":", ratesJson,
							ratesList, Settings.Rates.Groups.STOCK, isUseCompactView, isShowChange,
							updateTS));
		}
		if( wCells >= 3 || hCells >= 2 ){
			widget.addView(R.id.rates,
					buildRatesGroup(context, context.getString(R.string.title_forex) + ":", ratesJson,
							ratesList, Settings.Rates.Groups.FOREX, isUseCompactView, isShowChange,
							updateTS));
		}

		// Show updating timestamp
		final String updateTextPrefix = ( wCells >= 2 ? context.getString(R.string.text_updated) : "" );
		final int updateTextFormatFlags = android.text.format.DateUtils.FORMAT_SHOW_DATE
				| android.text.format.DateUtils.FORMAT_SHOW_TIME
				| android.text.format.DateUtils.FORMAT_ABBREV_ALL;
		final String updateText = updateTextPrefix + " "
				+ android.text.format.DateUtils.formatDateTime(context, updateTS.getTime(),
				updateTextFormatFlags);
		widget.setTextViewText(R.id.updateTimestamp, updateText);

		Intent appIntent = new Intent(context, MainActivity.class);
		PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
		widget.setOnClickPendingIntent(R.id.widget, appPendingIntent);

		return widget;
	}

	protected static RemoteViews buildRatesGroup(Context context, String caption,
	                                             JSONObject ratesJson, List<String> ratesList, String ratesGroup,
	                                             boolean isUseCompactView, boolean isShowChange, Date updateTS) {
		if( isUseCompactView ){
			isShowChange = false;
		}

		ExRatesGroup exRatesGroup = new ExRatesGroup(caption, ratesGroup, ratesList, ratesJson);

		exRatesGroup.isCompactTitleMode = isUseCompactView;
		exRatesGroup.isLongFormat = sIsLongFormat;

		exRatesGroup.calcViewsBounds(context);

		final Date groupTS = exRatesGroup.getUpdateTS();
		if( groupTS.after(updateTS) ){
			updateTS.setTime(groupTS.getTime());
		}

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final boolean invertColors = prefs.getBoolean(Settings.Preferences.INVERT_COLORS, false);

		return exRatesGroup.buildWidgetViews(context, isShowChange, invertColors);
	}
}

