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

package com.khrolenok.rates;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.khrolenok.rates.util.AnalyticsTrackers;
import com.khrolenok.rates.util.ConnectionMonitor;
import com.khrolenok.rates.util.PreferencesManager;
import com.khrolenok.rates.util.StockNames;
import com.khrolenok.rates.util.UpdateService;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

import trikita.log.Log;

/**
 * Created by Limych on 05.09.2015
 */
public class ExRatesApplication extends Application {

	private static ExRatesApplication mInstance;

	public static String deviceId;
	public static boolean isTestDevice = false;

	public static boolean isShowAds = BuildConfig.SHOW_ADS;

	public static final int MODE_RATES = 0;
	public static final int MODE_ABOUT = 1;

	public static int mode = MODE_RATES;

	static final public String ACTION_STOCKS_UPDATE = "com.khrolenok.rates.action.STOCKS_UPDATE";
	static final public String ERROR_NO_CONNECTION = "com.khrolenok.rates.conn.NO_CONNECTION";

	public static synchronized ExRatesApplication getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		if( BuildConfig.DEBUG && BuildConfig.STRICT_MODE ){
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectAll()
					.penaltyLog()
					.penaltyDialog()
					.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
					.penaltyLog()
					.build());
		}

		if( BuildConfig.GOOGLE_ANALYTICS ){
			AnalyticsTrackers.initialize(this);
			AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
		}

		initApplication(getApplicationContext());

		deviceId = getDeviceID();
		if( BuildConfig.DEBUG ) Log.d("deviceId = " + deviceId);

		isTestDevice = deviceId.equals(getMD5Hash("emulator"))          // SDK emulator
				|| deviceId.equals("731E71558550B5248AD569E9A603BBA7"); // My test device
//		isTestDevice = true;
		if( BuildConfig.DEBUG && isTestDevice ) Log.v("Test device detected");

		// Try to start update service
		UpdateService.notifyUpdateNeeded(getApplicationContext());
	}

	public static void initApplication(Context context){
		initPreferences(context);
		StockNames.getInstance().init(context);
	}

	public static void initPreferences(Context context) {
		PreferencesManager.getInstance().init(context);
		if( !PreferencesManager.getInstance().contains(PreferencesManager.PREF_STOCKS_LIST) ){
			ArrayList<String> stocksList = new ArrayList<>();
			stocksList.add("CBR_USD_RUB");
			stocksList.add("CBR_EUR_RUB");
			stocksList.add("STK_USD_RUB");
			stocksList.add("STK_EUR_RUB");
			stocksList.add("FRX_USD_RUB");
			stocksList.add("FRX_EUR_RUB");
			PreferencesManager.getInstance().setStocksList(stocksList);
		}
	}

	public String getDeviceID() {
		final ContentResolver contentResolver = this.getContentResolver();
		final String androidId = ( contentResolver == null
				? null : Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) );
		return getMD5Hash(( androidId != null && !Build.DEVICE.startsWith("generic")
				? androidId : "emulator" ));
	}

	public String getMD5Hash(String data) {
		int attempts = 0;

		while( attempts < 2 ){
			try{
				MessageDigest hash = MessageDigest.getInstance("MD5");
				hash.update(data.getBytes());
				return String.format(Locale.US, "%032X", new BigInteger(1, hash.digest()));
			} catch( NoSuchAlgorithmException ignored ){
				++attempts;
			}
		}

		return null;
	}

	public synchronized Tracker getAnalyticsTracker() {
		return AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
	}

	/***
	 * Tracking screen view
	 *
	 * @param screenName screen name to be displayed on GA dashboard
	 */
	public void trackScreenView(String screenName) {
		if( !BuildConfig.GOOGLE_ANALYTICS ) return;

		final Tracker t = getAnalyticsTracker();

		// Set screen name.
		t.setScreenName(screenName);

		// Send a screen view.
		t.send(new HitBuilders.ScreenViewBuilder().build());

		if( ConnectionMonitor.isNetworkAvailable(this) ){
			GoogleAnalytics.getInstance(this).dispatchLocalHits();
		}
	}

	/***
	 * Tracking exception
	 *
	 * @param e exception to be tracked
	 */
	public void trackException(Exception e) {
		if( !BuildConfig.GOOGLE_ANALYTICS ) return;

		if( e != null ){
			final Tracker t = getAnalyticsTracker();

			t.send(new HitBuilders.ExceptionBuilder()
							.setDescription(
									new StandardExceptionParser(this, null)
											.getDescription(Thread.currentThread().getName(), e))
							.setFatal(false)
							.build()
			);
		}
	}

	/***
	 * Tracking event
	 *
	 * @param category event category
	 * @param action   action of the event
	 */
	public void trackEvent(String category, String action) {
		trackEvent(category, action, null);
	}

	/***
	 * Tracking event
	 *
	 * @param category event category
	 * @param action   action of the event
	 * @param label    label
	 */
	public void trackEvent(String category, String action, String label) {
		if( !BuildConfig.GOOGLE_ANALYTICS ) return;

		final Tracker t = getAnalyticsTracker();

		// Build an Event
		final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
				.setCategory(category).setAction(action);
		if( label != null ) eventBuilder.setLabel(label);

		// Send an Event
		t.send(eventBuilder.build());
	}

	/***
	 * Tracking timing
	 *
	 * @param category category of the timed event
	 * @param timing   timing measurement (ms)
	 */
	public void trackTiming(String category, long timing) {
		trackTiming(category, timing, null, null);
	}

	/***
	 * Tracking timing
	 *
	 * @param category category of the timed event
	 * @param timing   timing measurement (ms)
	 * @param name     name of the timed event
	 */
	public void trackTiming(String category, long timing, String name) {
		trackTiming(category, timing, name, null);
	}

	/***
	 * Tracking timing
	 *
	 * @param category category of the timed event
	 * @param timing   timing measurement (ms)
	 * @param name     name of the timed event
	 * @param label    label of the timed event
	 */
	public void trackTiming(String category, long timing, String name, String label) {
		if( !BuildConfig.GOOGLE_ANALYTICS ) return;

		final Tracker t = getAnalyticsTracker();

		// Build an Event
		final HitBuilders.TimingBuilder timingBuilder = new HitBuilders.TimingBuilder()
				.setCategory(category).setValue(timing);
		if( name != null ) timingBuilder.setVariable(name);
		if( label != null ) timingBuilder.setLabel(label);

		// Send an Event
		t.send(timingBuilder.build());
	}
}
