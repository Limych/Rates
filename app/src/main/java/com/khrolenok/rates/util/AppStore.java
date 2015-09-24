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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import java.util.List;

/**
 * Created by Limych on 24.09.2015
 */
public class AppStore {

	public static final int UNKNOWN = 0;
	public static final int GOOGLE_PLAY = 1;
	public static final int AMAZON = 2;
	public static final int SAMSUNG = 3;
	public static final int OTHER = -1;

	public static String installer;

	public static int detect(Context context) {
		final PackageManager pm = context.getPackageManager();
		installer = pm.getInstallerPackageName(context.getPackageName());

		if( installer == null ){
			List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
			for( PackageInfo p : installedPackages ) {
				if( p.packageName.contains("samsungapps") ) return SAMSUNG;
			}
			return UNKNOWN;

		} else if( installer.equalsIgnoreCase("com.android.vending") )
			return GOOGLE_PLAY;

		else if( Build.MANUFACTURER.equalsIgnoreCase("amazon") || installer.equalsIgnoreCase("com.amazon.venezia") )
			return AMAZON;

		else if( installer.equalsIgnoreCase("com.sec.android.app.samsungapps") )
			return SAMSUNG;

		return OTHER;
	}

	public static void openMarket(Context context, int market) {
		switch( market ){
			case GOOGLE_PLAY:
				final String packageName = context.getPackageName();
				try{
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
				} catch( android.content.ActivityNotFoundException ignore ){
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
				}
				break;
		}
	}
}
