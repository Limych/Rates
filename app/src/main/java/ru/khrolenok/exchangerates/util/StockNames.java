package ru.khrolenok.exchangerates.util;

import android.content.Context;

import java.util.HashMap;

import ru.khrolenok.exchangerates.R;

/**
 * Created by Limych on 05.09.2015.
 */
public class StockNames {

	private HashMap<String,String> mNamesMap;

	private static StockNames stockNames;

	public static StockNames getInstance() {
		if (stockNames == null) {
			stockNames = new StockNames();
		}
		return stockNames;
	}

	public void init(Context context) {
		mNamesMap = new HashMap<String,String>();

		final String[] stockNames = context.getResources().getStringArray(R.array.stockNames);

		for( String item : stockNames ) {
			final String[] parts = item.split("\\s+[-—]\\s+", 2);
			final String[] symbols = parts[0].split(",\\s*");
			for( String symbol : symbols ) {
				mNamesMap.put(symbol, parts[1]);
			}
		}
	}

	public String getName(String symbol){
		return mNamesMap.get(symbol);
	}
}
