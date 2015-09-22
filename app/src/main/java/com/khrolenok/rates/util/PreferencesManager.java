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

	// Public preferences
	public static final String PREF_STOCKS_LIST = "stocksList";
	public static final String PREF_INVERT_COLORS = "invert_colors";
	public static final String PREF_LONG_FORMAT = "long_format";
	public static final String PREF_WIFI_ONLY = "wifi_only";

	// Private preferences
	public static final String PREF_STOCKS_DATA = "_stocksData";
	public static final String PREF_UPDATE_TIME = "_updateTime";

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

	public boolean contains(String key) {
		return tinyDB.contains(key);
	}

	public void addStockSymbol(String stockSymbol) {
		ArrayList<String> stocksList = tinyDB.getListString(PREF_STOCKS_LIST);
		stocksList.add(stockSymbol);
		tinyDB.putListString(PREF_STOCKS_LIST, stocksList);
	}

	public void removeStockSymbol(String stockSymbol) {
		ArrayList<String> stocksList = tinyDB.getListString(PREF_STOCKS_LIST);
		stocksList.remove(stockSymbol);
		tinyDB.putListString(PREF_STOCKS_LIST, stocksList);
	}

	public boolean stocksListContains(String stockSymbol) {
		ArrayList<String> stocksList = tinyDB.getListString(PREF_STOCKS_LIST);
		return stocksList.contains(stockSymbol);
	}

	public ArrayList<String> getStocksList() {
		return tinyDB.getListString(PREF_STOCKS_LIST);
	}

	public void setStocksList(ArrayList<String> stocksList) {
		tinyDB.putListString(PREF_STOCKS_LIST, stocksList);
	}

	public void setBoolean(String key, boolean value) {
		tinyDB.putBoolean(key, value);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return tinyDB.getBoolean(key, defaultValue);
	}

	public void setStockData(String stocksData) {
		tinyDB.putString(PREF_STOCKS_DATA, stocksData);
	}

	public String getStockData() {
		return tinyDB.getString(PREF_STOCKS_DATA);
	}

	public void setUpdateTime(long value) {
		tinyDB.putLong(PREF_UPDATE_TIME, value);
	}

	public long getUpdateTime() {
		return tinyDB.getLong(PREF_UPDATE_TIME, 0);
	}
}
