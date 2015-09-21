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

import com.khrolenok.rates.util.PreferencesManager;
import com.khrolenok.rates.util.StockNames;
import com.khrolenok.rates.util.UpdateService;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

import trikita.log.Log;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Limych on 05.09.2015
 */
public class ExRatesApplication extends Application {

	private static final String FONT_PATH = "fonts/Roboto-Regular.ttf";

	public static String deviceId;
	public static boolean isTestDevice = false;

	public static boolean isShowAds = BuildConfig.SHOW_ADS;

	public static final int MODE_RATES = 0;
	public static final int MODE_ABOUT = 1;

	public static int mode = MODE_RATES;

	@Override
	public void onCreate() {
		super.onCreate();

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

		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
						.setDefaultFontPath(FONT_PATH)
						.setFontAttrId(R.attr.fontPath)
						.build()
		);

		initPreferences(getApplicationContext());

		StockNames.getInstance().init(getApplicationContext());

		deviceId = getDeviceID();
		if( BuildConfig.DEBUG ) Log.d("deviceId = " + deviceId);

		isTestDevice = deviceId.equals(getMD5Hash("emulator"))          // SDK emulator
				|| deviceId.equals("731E71558550B5248AD569E9A603BBA7"); // My test device
//		isTestDevice = true;
		if( BuildConfig.DEBUG && isTestDevice ) Log.v("Test device detected");

		// Try to start update service
		UpdateService.start(getApplicationContext());
	}

	public static void initPreferences(Context context) {
		PreferencesManager.getInstance().init(context);
		if (!PreferencesManager.getInstance().contains(PreferencesManager.PREF_STOCKS_LIST)) {
			ArrayList<String> stocksList = new ArrayList<String>();
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
}
