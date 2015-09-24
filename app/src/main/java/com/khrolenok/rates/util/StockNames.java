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

import com.khrolenok.rates.R;

import java.util.HashMap;

/**
 * Created by Limych on 05.09.2015
 */
public class StockNames {

	private HashMap<String,String> mNamesMap;
	private HashMap<String,String> mShortCodes;

	private static StockNames stockNames;

	public static StockNames getInstance() {
		if (stockNames == null) {
			stockNames = new StockNames();
		}
		return stockNames;
	}

	public void init(Context context) {
		String[] stockNames;

		mNamesMap = new HashMap<>();
		stockNames = context.getResources().getStringArray(R.array.stockNames);
		for( String item : stockNames ) {
			final String[] parts = item.split("\\s*[-—=]\\s*", 2);
			final String[] symbols = parts[0].split(",\\s*");
			for( String symbol : symbols ) {
				mNamesMap.put(symbol, parts[1]);
			}
		}

		mShortCodes = new HashMap<>();
		stockNames = context.getResources().getStringArray(R.array.stockShortCodes);
		for( String item : stockNames ) {
			final String[] parts = item.split("\\s*[-—=]\\s*", 2);
			final String[] symbols = parts[0].split(",\\s*");
			for( String symbol : symbols ) {
				mShortCodes.put(symbol, parts[1]);
			}
		}
	}

	public String getName(String code){
		return mNamesMap.get(code);
	}

	public String getShortCode(String code){
		return mShortCodes.get(code);
	}
}
