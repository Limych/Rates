/*
 * Copyright (c) 2015. Andrey “Limych” Khrolenok
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

package ru.khrolenok.exchangerates;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.Settings;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import ru.khrolenok.exchangerates.ui.WidgetProvider;
import ru.khrolenok.exchangerates.util.StockNames;
import trikita.log.Log;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Limych on 05.09.2015
 */
public class ExRatesApplication extends Application {

	private static final String FONT_PATH = "fonts/Roboto-Regular.ttf";

	public static String deviceId;
	public static boolean isTestDevice = false;

	@Override
	public void onCreate() {
		super.onCreate();

		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
						.setDefaultFontPath(FONT_PATH)
						.setFontAttrId(R.attr.fontPath)
						.build()
		);
		StockNames.getInstance().init(getApplicationContext());

		deviceId = getDeviceID();
		Log.d("deviceId = " + deviceId);

		isTestDevice = deviceId.equals(getMD5Hash("emulator"))          // SDK emulator
				|| deviceId.equals("731E71558550B5248AD569E9A603BBA7"); // My test device
//		isTestDevice = true;
		if( isTestDevice ) Log.v("Test device detected");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		StockNames.getInstance().init(getApplicationContext());

		// TODO: 11.09.2015 Make correct widgets updating
		Intent intent = new Intent(this, WidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		final int widgetIds[] = AppWidgetManager.getInstance(this)
				.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		sendBroadcast(intent);
	}

	public String getDeviceID() {
		final ContentResolver contentResolver = this.getContentResolver();
		final String androidId = ( contentResolver == null
				? null : Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) );
		return getMD5Hash(( androidId != null && !this.isEmulator() ? androidId : "emulator" ));
	}

	public boolean isEmulator() {
		return Build.DEVICE.startsWith("generic");
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
