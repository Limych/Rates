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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khrolenok.rates.R;
import com.khrolenok.rates.Settings;
import com.khrolenok.rates.model.StockItem;
import com.khrolenok.rates.ui.MainActivity;
import com.khrolenok.rates.ui.RatesFragment;
import com.khrolenok.rates.util.PreferencesManager;
import com.khrolenok.rates.util.StockNames;

import java.util.Date;
import java.util.List;

/**
 * Created by Limych on 17.09.2015
 */
public class StockItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_FOOTER = Integer.MIN_VALUE + 1;

	private List<StockItem> mStockItems;
	private final Context mContext;
	private final RatesFragment mFragment;

	public StockItemsAdapter(Context context, List<StockItem> stockItems, RatesFragment fragment) {
		mContext = context;
		mFragment = fragment;
		setStockItems(stockItems);
	}

	public void setStockItems(List<StockItem> stockItems) {
		this.mStockItems = stockItems;
	}

	@Override
	public int getItemCount() {
		return ( mStockItems == null ? 0 : mStockItems.size() + 1 );
	}

	@Override
	public int getItemViewType(int position) {
		if( position == getItemCount() - 1 ) return TYPE_FOOTER;
		return super.getItemViewType(position);
	}

	public StockItem getItem(int position) {
		if( position < getItemCount() - 1 ) return mStockItems.get(position);
		return null;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		switch( viewType ){
			case TYPE_FOOTER:
				return new FooterViewHolder(inflater.inflate(R.layout.fragment_rates_footer, parent, false));

			default:
				return new ViewHolder(inflater.inflate(R.layout.fragment_rates_item, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		if( getItemViewType(position) == TYPE_FOOTER ){
			final FooterViewHolder vh = (FooterViewHolder) viewHolder;
			Date updateTS = new Date(0);
			for( StockItem item : mStockItems ) {
				if( item.lastUpdate != null && item.lastUpdate.after(updateTS) )
					updateTS.setTime(item.lastUpdate.getTime());
			}
			vh.setUpdateTS(updateTS);

		} else {
			final ViewHolder vh = (ViewHolder) viewHolder;
			final StockItem stockItem = mStockItems.get(position);
			vh.setItemData(position, stockItem);
		}
	}

	public void fillValuesFromMainValue(double mainValue) {
		( (MainActivity) mContext ).mainValue = mainValue;
		for( StockItem si : mStockItems ) {
			si.value = mainValue * si.faceValue / si.lastPrice;
		}
	}

	public class ViewHolder extends RecyclerView.ViewHolder {

		private TextView symbolTV;
		private TextView nameTV;
		private TextView priceTV;
		private TextView priceChangeDirTV;
		private TextView priceChangeTV;
		private TextView valueTV;
		private ViewGroup valueContainer;

		public ViewHolder(View itemView) {
			super(itemView);

			symbolTV = (TextView) itemView.findViewById(R.id.itemSymbol);
			nameTV = (TextView) itemView.findViewById(R.id.itemName);
			priceTV = (TextView) itemView.findViewById(R.id.itemPrice);
			priceChangeDirTV = (TextView) itemView.findViewById(R.id.itemChangeDirection);
			priceChangeTV = (TextView) itemView.findViewById(R.id.itemChange);
			valueTV = (TextView) itemView.findViewById(R.id.itemValue);
			valueContainer = (ViewGroup) itemView.findViewById(R.id.itemValueContainer);
		}

		public String getStockExchangeName(Context context, String group) {
			if( group == null ){
				return "";
			}
			String stockExchangeName;
			switch( group ){
				case Settings.Rates.Groups.OFFICIAL:
					stockExchangeName = context.getString(R.string.title_official);
					break;
				case Settings.Rates.Groups.FOREX:
					stockExchangeName = context.getString(R.string.title_forex);
					break;
				case Settings.Rates.Groups.STOCK:
					stockExchangeName = context.getString(R.string.title_stock);
					break;
				default:
					stockExchangeName = group;
					break;
			}
			return "(" + stockExchangeName + ") ";
		}

		public void setItemData(final int position, final StockItem stockItem) {
			final Context context = itemView.getContext();
			final PreferencesManager prefs = PreferencesManager.getInstance();
			final boolean isLongFormat = prefs.getBoolean(PreferencesManager.PREF_LONG_FORMAT, false);


			symbolTV.setText(stockItem.symbol);
			nameTV.setText(getStockExchangeName(context, stockItem.stockExchange)
					+ StockNames.getInstance().getTitle(stockItem.symbol));
			valueTV.setText(stockItem.getValueFormatted());
			priceChangeTV.setText(stockItem.getPriceChangeFormatted(isLongFormat));

			if( stockItem.symbol.equals(stockItem.priceCurrency) ){
				priceTV.setVisibility(View.GONE);
				priceChangeDirTV.setVisibility(View.GONE);
				priceChangeTV.setVisibility(View.GONE);
			} else {
				priceTV.setVisibility(View.VISIBLE);
				priceChangeDirTV.setVisibility(View.VISIBLE);
				priceChangeTV.setVisibility(View.VISIBLE);
				priceTV.setText(stockItem.getLastPriceFormatted(isLongFormat));
				priceChangeDirTV.setText(( !stockItem.hasPriceChange()
						? R.string.change_direction_none
						: ( stockItem.getPriceChange() > 0 ? R.string.change_direction_up : R.string.change_direction_down ) ));
			}

			if( stockItem.hasPriceChange() ){
				final boolean invertColors = prefs.getBoolean(PreferencesManager.PREF_INVERT_COLORS, false);
				final int colorUp = ( !invertColors ? R.color.change_green : R.color.change_red );
				final int colorDown = ( !invertColors ? R.color.change_red : R.color.change_green );
				final int color = context.getResources().getColor(( stockItem.getPriceChange() > 0
						? colorUp : colorDown ));
				priceTV.setTextColor(color);
				priceChangeDirTV.setTextColor(color);
				priceChangeTV.setTextColor(color);
			}

			valueContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mFragment.showCalculatorDialog(position, (String) nameTV.getText(),
							stockItem.value, stockItem.symbol);
				}
			});
		}
	}

	public class FooterViewHolder extends RecyclerView.ViewHolder {

		private TextView updateTV;

		public FooterViewHolder(View itemView) {
			super(itemView);

			updateTV = (TextView) itemView.findViewById(R.id.updateTimestamp);
		}

		public void setUpdateTS(Date updateTS) {
			final Context context = itemView.getContext();
			final int updateTextFormatFlags = android.text.format.DateUtils.FORMAT_SHOW_DATE
					| android.text.format.DateUtils.FORMAT_SHOW_TIME
					| android.text.format.DateUtils.FORMAT_ABBREV_ALL;
			final String updateText = context.getString(R.string.text_updated) + " "
					+ android.text.format.DateUtils.formatDateTime(context, updateTS.getTime(),
					updateTextFormatFlags);
			updateTV.setText(updateText);
		}
	}
}
