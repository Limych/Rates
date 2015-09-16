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

package com.khrolenok.rates.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.khrolenok.rates.ExRate;
import com.khrolenok.rates.R;
import com.khrolenok.rates.Settings;
import com.khrolenok.rates.model.StockItem;
import com.khrolenok.rates.ui.MainActivity;
import com.khrolenok.rates.ui.MainFragment;
import com.khrolenok.rates.util.StockNames;
import com.nhaarman.listviewanimations.util.Swappable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Limych on 04.09.2015
 */
public class StockItemsAdapter extends ArrayAdapter<StockItem> implements Swappable {

	final private ArrayList<StockItem> mStockItems;
	final private MainFragment mFragment;

	private static final int VIEW_TYPE_RATE = 1;
	private static final int VIEW_TYPE_FOOTER = 2;

	private static class ViewHolder {
		private TextView symbolTV;
		private TextView nameTV;
		private TextView priceTV;
		private TextView priceChangeDirTV;
		private TextView priceChangeTV;
		private TextView valueTV;
		private ViewGroup valueContainer;

		public ViewHolder(View itemView) {
			symbolTV = (TextView) itemView.findViewById(R.id.itemSymbol);
			nameTV = (TextView) itemView.findViewById(R.id.itemName);
			priceTV = (TextView) itemView.findViewById(R.id.itemPrice);
			priceChangeDirTV = (TextView) itemView.findViewById(R.id.itemChangeDirection);
			priceChangeTV = (TextView) itemView.findViewById(R.id.itemChange);
			valueTV = (TextView) itemView.findViewById(R.id.itemValue);
			valueContainer = (ViewGroup) itemView.findViewById(R.id.itemValueContainer);
		}
	}

	public StockItemsAdapter(Context context, ArrayList<StockItem> stockItems,
	                         MainFragment fragment) {
		super(context, R.layout.fragment_main_item, stockItems);
		mStockItems = stockItems;
		mFragment = fragment;
	}

	public void fillFromExRates(List<ExRate> exRates) {
		final double mainValue = ( (MainActivity) getContext() ).mainValue;

		StockItem item = new StockItem();

		item.code = "RUR";
		item.symbol = "RUR";
		item.faceValue = 1;
		item.priceCurrency = "RUR";
		item.initialPrice = 1;
		item.lastPrice = 1;
		item.value = mainValue;

		mStockItems.add(item);

		for( int i = 0, cnt = exRates.size(); i < cnt; i++ ) {
			final ExRate exRate = exRates.get(i);
			item = new StockItem();

			item.code = exRate.groupCode + "_" + exRate.goodCode + "_" + exRate.currencyCode;
			item.stockExchange = exRate.groupCode;
			item.symbol = exRate.goodCode;
			item.faceValue = exRate.faceValue;
			item.priceCurrency = exRate.currencyCode;
			item.initialPrice = exRate.initialBid;
			item.lastPrice = exRate.lastBid;
			item.lastUpdate = exRate.updateTS;
			item.value = mainValue * item.faceValue / item.lastPrice;

			mStockItems.add(item);
		}
	}

	public void fillValuesFromMainValue(double mainValue) {
		( (MainActivity) getContext() ).mainValue = mainValue;
		for( StockItem si : mStockItems ) {
			si.value = mainValue * si.faceValue / si.lastPrice;
		}
	}

	public View getFooterView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.fragment_main_footer, null, false);

		Date updateTS = new Date(0);

		for( StockItem item : mStockItems ) {
			if( item.lastUpdate != null && item.lastUpdate.after(updateTS) )
				updateTS.setTime(item.lastUpdate.getTime());
		}

		TextView updateTV = (TextView) view.findViewById(R.id.updateTimestamp);
		final int updateTextFormatFlags = android.text.format.DateUtils.FORMAT_SHOW_DATE
				| android.text.format.DateUtils.FORMAT_SHOW_TIME
				| android.text.format.DateUtils.FORMAT_ABBREV_ALL;
		final String updateText = getContext().getString(R.string.text_updated) + " "
				+ android.text.format.DateUtils.formatDateTime(getContext(), updateTS.getTime(),
				updateTextFormatFlags);
		updateTV.setText(updateText);
		return view;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final StockItem stockItem = getItem(position);
		final ViewHolder viewHolder;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		final boolean isLongFormat = prefs.getBoolean(Settings.Preferences.LONG_FORMAT, false);

		if( convertView != null ){
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.fragment_main_item, parent, false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		}

		viewHolder.symbolTV.setText(stockItem.symbol);
		viewHolder.nameTV.setText(getStockExchangeName(stockItem.stockExchange)
				+ StockNames.getInstance().getName(stockItem.symbol));
		viewHolder.valueTV.setText(stockItem.getValueFormatted());
		viewHolder.priceChangeTV.setText(stockItem.getPriceChangeFormatted(isLongFormat));

		if( stockItem.symbol.equals(stockItem.priceCurrency) ){
			viewHolder.priceTV.setVisibility(View.GONE);
			viewHolder.priceChangeDirTV.setVisibility(View.GONE);
			viewHolder.priceChangeTV.setVisibility(View.GONE);
		} else {
			viewHolder.priceTV.setVisibility(View.VISIBLE);
			viewHolder.priceChangeDirTV.setVisibility(View.VISIBLE);
			viewHolder.priceChangeTV.setVisibility(View.VISIBLE);
			viewHolder.priceTV.setText(stockItem.getLastPriceFormatted(isLongFormat));
			viewHolder.priceChangeDirTV.setText(( !stockItem.hasPriceChange()
					? R.string.change_direction_none
					: ( stockItem.getPriceChange() > 0 ? R.string.change_direction_up : R.string.change_direction_down ) ));
		}

		if( stockItem.hasPriceChange() ){
			final boolean invertColors = prefs.getBoolean(Settings.Preferences.INVERT_COLORS, false);
			final int colorUp = ( !invertColors ? R.color.change_green : R.color.change_red );
			final int colorDown = ( !invertColors ? R.color.change_red : R.color.change_green );
			final int color = getContext().getResources().getColor(( stockItem.getPriceChange() > 0
					? colorUp : colorDown ));
			viewHolder.priceTV.setTextColor(color);
			viewHolder.priceChangeDirTV.setTextColor(color);
			viewHolder.priceChangeTV.setTextColor(color);
		}

		viewHolder.valueContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFragment.showCalculatorDialog(position, (String) viewHolder.nameTV.getText(), stockItem.value);
			}
		});

		return convertView;
	}

	public String getStockExchangeName(String group) {
		if( group == null ){
			return "";
		}
		String stockExchangeName;
		switch( group ){
			case Settings.Rates.Groups.OFFICIAL:
				stockExchangeName = getContext().getString(R.string.title_official);
				break;
			case Settings.Rates.Groups.FOREX:
				stockExchangeName = getContext().getString(R.string.title_forex);
				break;
			case Settings.Rates.Groups.STOCK:
				stockExchangeName = getContext().getString(R.string.title_stock);
				break;
			default:
				stockExchangeName = group;
				break;
		}
		return "(" + stockExchangeName + ") ";
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void swapItems(int i1, int i2) {
		Collections.swap(mStockItems, i1, i2);
		notifyDataSetChanged();
	}

	public void saveOrder() {
		ArrayList<String> stocksList = new ArrayList<>();
		for( StockItem item : mStockItems ) {
			stocksList.add(item.symbol);
		}
		// TODO: 15.09.2015 Save order
//        PreferencesManager.getInstance().saveStockList(stocksList);
	}

	public void orderByAlphabet() {
		Collections.sort(mStockItems);
	}
}
