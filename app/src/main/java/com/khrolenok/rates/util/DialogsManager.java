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
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.khrolenok.rates.R;

import me.drakeet.materialdialog.MaterialDialog;

public class DialogsManager {

//	private static Map<String, String> usedLibraries = new HashMap();
//
//	static {
//		//All used open source libraries
//		usedLibraries.put("Trikita.Log", "https://github.com/zserge/log");
//		usedLibraries.put("Calligraphy", "https://github.com/chrisjenx/Calligraphy");
//		usedLibraries.put("NineOldAndroids", "https://github.com/JakeWharton/NineOldAndroids/");
//		usedLibraries.put("ListViewAnimations", "https://github.com/nhaarman/ListViewAnimations");
//		usedLibraries.put("MaterialDialog", "https://github.com/drakeet/MaterialDialog");
////		usedLibraries.put("MaterialDesignLibrary", "https://github.com/navasmdc/MaterialDesignLibrary");
////		usedLibraries.put("MPAndroidChart", "https://github.com/PhilJay/MPAndroidChart");
////		usedLibraries.put("AndroidViewAnimations", "https://github.com/daimajia/AndroidViewAnimations");
//
//	}

	public static void showAboutDialog(Context context) {
//		StringBuilder stringBuilder = new StringBuilder("<b>Open source libraries:</b><br>");
//		for( Map.Entry<String, String> entry : usedLibraries.entrySet() ) {
//			stringBuilder.append("<a href='" + entry.getValue() + "'>" + entry.getKey() + "<a><br>");
//		}
//		stringBuilder.append("<br><b>Free icons:</b><br>");
//		stringBuilder.append("<a href='http://icons8.com/android-L/'" + ">icons8<a>");

		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View contentView = inflater.inflate(R.layout.dialog_about, null);

		// Set the application version
		String versionName = "";
		try{
			versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch( PackageManager.NameNotFoundException ignored ){
		}
		final TextView text = (TextView) contentView.findViewById(R.id.app_version);
		text.setText(text.getText() + " " + versionName);

		final MaterialDialog mMaterialDialog = new MaterialDialog(context);
		mMaterialDialog.setCanceledOnTouchOutside(true);
		mMaterialDialog.setContentView(contentView);
		mMaterialDialog.show();
	}

}
