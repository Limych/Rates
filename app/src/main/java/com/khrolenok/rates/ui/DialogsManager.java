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

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.khrolenok.rates.BuildConfig;
import com.khrolenok.rates.ExRatesApplication;
import com.khrolenok.rates.R;

public class DialogsManager {

//	private static Map<String, String> usedLibraries = new HashMap();
//
//	static {
//		//All used open source libraries
//		usedLibraries.put("Trikita.Log", "https://github.com/zserge/log");
//		usedLibraries.put("Calligraphy", "https://github.com/chrisjenx/Calligraphy");
//		usedLibraries.put("NineOldAndroids", "https://github.com/JakeWharton/NineOldAndroids/");
//		usedLibraries.put("ListViewAnimations", "https://github.com/nhaarman/ListViewAnimations");
////		usedLibraries.put("MaterialDialog", "https://github.com/drakeet/MaterialDialog");
////		usedLibraries.put("MaterialDesignLibrary", "https://github.com/navasmdc/MaterialDesignLibrary");
////		usedLibraries.put("MPAndroidChart", "https://github.com/PhilJay/MPAndroidChart");
////		usedLibraries.put("AndroidViewAnimations", "https://github.com/daimajia/AndroidViewAnimations");
//
//	}

	public static void showAboutDialog(Context context) {
		ExRatesApplication.mode = ExRatesApplication.MODE_ABOUT;

//		StringBuilder stringBuilder = new StringBuilder("<b>Open source libraries:</b><br>");
//		for( Map.Entry<String, String> entry : usedLibraries.entrySet() ) {
//			stringBuilder.append("<a href='" + entry.getValue() + "'>" + entry.getKey() + "<a><br>");
//		}
//		stringBuilder.append("<br><b>Free icons:</b><br>");
//		stringBuilder.append("<a href='http://icons8.com/android-L/'" + ">icons8<a>");

		final View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_about, null, false);

		// Set the application version
		String versionName = "";
		try{
			versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch( PackageManager.NameNotFoundException ignored ){
		}
		if( BuildConfig.DEBUG ) versionName += " (debug)";
		final TextView text = (TextView) contentView.findViewById(R.id.app_version);
		text.setText(text.getText() + " " + versionName);

		new AlertDialog.Builder(context)
				.setCancelable(true)
				.setOnCancelListener(new DialogInterface.OnCancelListener(){

					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						ExRatesApplication.mode = ExRatesApplication.MODE_RATES;
					}
				})
				.setView(contentView)
				.create().show();
	}

	public static void applyWorkaroundForButtonWidthsTooWide(AlertDialog dialog) {
		final Button dialogButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

		if( dialogButton == null )
			return;
		if( !( dialogButton.getParent() instanceof LinearLayout ) )
			return;

		// Workaround for buttons too large in alternate languages.
		final LinearLayout linearLayout = (LinearLayout) dialogButton.getParent();
		linearLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
			                           int oldRight, int oldBottom) {
				if( right - left > 0 ){
					final int parentWidth = linearLayout.getWidth();
					int childrenWidth = 0;
					for( int i = 0; i < linearLayout.getChildCount(); ++i )
						childrenWidth += linearLayout.getChildAt(i).getWidth();

					if( childrenWidth > parentWidth ){
						// Apply stacked buttons
						linearLayout.setOrientation(LinearLayout.VERTICAL);
						linearLayout.setPadding(linearLayout.getPaddingLeft(), 0, linearLayout.getPaddingRight(),
								linearLayout.getPaddingBottom());
						for( int i = 0; i < linearLayout.getChildCount(); ++i ) {
							if( linearLayout.getChildAt(i) instanceof Button ){
								final Button child = (Button) linearLayout.getChildAt(i);
								child.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
								final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
								params.width = LinearLayout.LayoutParams.MATCH_PARENT;
								params.gravity = Gravity.END;
								child.setLayoutParams(params);
							} else if( linearLayout.getChildAt(i) instanceof Space ){
								linearLayout.removeViewAt(i--);
							}
						}
					}

					linearLayout.removeOnLayoutChangeListener(this);
				}
			}
		});
	}
}
