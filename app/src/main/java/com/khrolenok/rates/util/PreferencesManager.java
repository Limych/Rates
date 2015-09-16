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

import java.util.ArrayList;

public class PreferencesManager {

	public static final String PREF_RATES_LIST = "rates_list";
	public static final String PREF_INVERT_COLORS = "invert_colors";
	public static final String PREF_LONG_FORMAT = "long_format";

	private static PreferencesManager prefsManager;
	private TinyDB tinyDB;


	public static PreferencesManager getInstance() {
		if( prefsManager == null ){
			prefsManager = new PreferencesManager();
		}
		return prefsManager;
	}

	public void init(Context context) {
		tinyDB = new TinyDB(context);
	}

	public void addStockSymbolToPrefs(String stockSymbol) {
		ArrayList<String> stocksList = tinyDB.getListString(PREF_RATES_LIST);
		stocksList.add(stockSymbol);
		tinyDB.putListString(PREF_RATES_LIST, stocksList);
	}

	public void removeStockSymbolFromPrefs(String stockSymbol) {
		ArrayList<String> stocksList = tinyDB.getListString(PREF_RATES_LIST);
		stocksList.remove(stockSymbol);
		tinyDB.putListString(PREF_RATES_LIST, stocksList);
	}

	public boolean stocksSetContains(String stockSymbol) {
		ArrayList<String> stocksList = tinyDB.getListString(PREF_RATES_LIST);
		return stocksList.contains(stockSymbol);
	}

	public void saveStockList(ArrayList<String> stocksList) {
		tinyDB.putListString(PREF_RATES_LIST, stocksList);
	}

	public void saveBoolean(String key, boolean value) {
		tinyDB.putBoolean(key, value);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return tinyDB.getBoolean(key, defaultValue);
	}

	public boolean hasBoolean(String key) {
		return tinyDB.contains(key);
	}

	public ArrayList<String> getStockList() {
		return tinyDB.getListString(PREF_RATES_LIST);
	}

	public boolean contains(String key) {
		return tinyDB.contains(key);
	}
}