package ru.khrolenok.exchangerates.controller;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhaarman.listviewanimations.util.Swappable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.khrolenok.exchangerates.ExRate;
import ru.khrolenok.exchangerates.R;
import ru.khrolenok.exchangerates.Settings;
import ru.khrolenok.exchangerates.model.StockItem;
import ru.khrolenok.exchangerates.ui.MainFragment;
import ru.khrolenok.exchangerates.util.StockNames;

/**
 * Created by Limych on 04.09.2015.
 */
public class StockItemsAdapter extends ArrayAdapter<StockItem>
		implements Swappable {

	final private Context mContext;
	final private ArrayList<StockItem> mStockItems;
	final private MainFragment mFragment;

	public StockItemsAdapter(Context context, ArrayList<StockItem> stockItems,
	                         MainFragment fragment) {
		super(context, R.layout.fragment_main_item, stockItems);
		mContext = context;
		mStockItems = stockItems;
		mFragment = fragment;
	}

	public void fillFromExRates(List<ExRate> exRates) {
		final double mainValue = 1000;

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
		for( StockItem si : mStockItems ) {
			si.value = mainValue * si.faceValue / si.lastPrice;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final StockItem stockItem = getItem(position);
		final ViewHolder viewHolder;

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
		viewHolder.priceChangeTV.setText(stockItem.getPriceChangeFormatted());

		if( stockItem.symbol.equals(stockItem.priceCurrency) ){
			viewHolder.priceTV.setVisibility(View.GONE);
			viewHolder.priceChangeDirTV.setVisibility(View.GONE);
			viewHolder.priceChangeTV.setVisibility(View.GONE);
		} else {
			viewHolder.priceTV.setText(stockItem.getLastPriceFormatted());
			viewHolder.priceChangeDirTV.setText(( !stockItem.hasPriceChange()
					? R.string.change_direction_none
					: ( stockItem.getPriceChange() > 0 ? R.string.change_direction_up : R.string.change_direction_down ) ));
		}

		if( stockItem.hasPriceChange() ){
			final boolean invertColors = PreferenceManager.getDefaultSharedPreferences(mContext)
					.getBoolean(Settings.Preferences.invertColors, false);
			final int colorUp = ( !invertColors ? R.color.change_green : R.color.change_red );
			final int colorDown = ( !invertColors ? R.color.change_red : R.color.change_green );
			final int color = mContext.getResources().getColor(( stockItem.getPriceChange() > 0
					? colorUp : colorDown ));
			viewHolder.priceTV.setTextColor(color);
			viewHolder.priceChangeDirTV.setTextColor(color);
			viewHolder.priceChangeTV.setTextColor(color);
		}

		viewHolder.valueTV.setOnClickListener(new View.OnClickListener() {
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
				stockExchangeName = mContext.getString(R.string.title_official);
				break;
			case Settings.Rates.Groups.FOREX:
				stockExchangeName = mContext.getString(R.string.title_forex);
				break;
			case Settings.Rates.Groups.STOCK:
				stockExchangeName = mContext.getString(R.string.title_stock);
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
		ArrayList<String> stocksList = new ArrayList<String>();
		for( StockItem item : mStockItems ) {
			stocksList.add(item.symbol);
		}
//        PreferencesManager.getInstance().saveStockList(stocksList);
	}

	public void orderByAlphabet() {
		Collections.sort(mStockItems);
	}

	private static class ViewHolder {
		private TextView symbolTV;
		private TextView nameTV;
		private TextView priceTV;
		private TextView priceChangeDirTV;
		private TextView priceChangeTV;
		private TextView valueTV;

		public ViewHolder(View itemView) {
			symbolTV = (TextView) itemView.findViewById(R.id.itemSymbol);
			nameTV = (TextView) itemView.findViewById(R.id.itemName);
			priceTV = (TextView) itemView.findViewById(R.id.itemPrice);
			priceChangeDirTV = (TextView) itemView.findViewById(R.id.itemChangeDirection);
			priceChangeTV = (TextView) itemView.findViewById(R.id.itemChange);
			valueTV = (TextView) itemView.findViewById(R.id.itemValue);
		}
	}
}
