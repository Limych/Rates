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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Limych on 18.09.2015
 */
public class ConnectionMonitor extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
			return;

		boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		if (!noConnectivity && isNetworkAvailable(context)) {

		}
	}

	public static void setEnabledSetting(Context context, int setting){
		final ComponentName receiver = new ComponentName(context, ConnectionMonitor.class);
		final PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver, setting, 0);
	}

	private static boolean isNetworkAvailable(Context context) {
		return isNetworkAvailable(context, false);
	}

	private static boolean isNetworkAvailable(Context context, boolean testForWiFi) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting()
				&& ( !testForWiFi || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI );
	}
}
