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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.khrolenok.rates.ExRate;
import com.khrolenok.rates.ExRatesGroup;
import com.khrolenok.rates.R;
import com.khrolenok.rates.Settings;
import com.khrolenok.rates.controller.StockItemsAdapter;
import com.khrolenok.rates.model.StockItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Limych on 07.09.2015
 */
public class RatesFragment extends Fragment
		implements SwipeRefreshLayout.OnRefreshListener, AppBarLayout.OnOffsetChangedListener {

	private RecyclerView mRatesListView;

	private StockItemsAdapter mStockItemsAdapter;
	private SwipeRefreshLayout srQuotesRefresher;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_rates, container, false);

		srQuotesRefresher = (SwipeRefreshLayout) rootView.findViewById(R.id.srQuotesRefresher);
		srQuotesRefresher.setOnRefreshListener(this);

		return rootView;
	}

	@Override
	public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
		srQuotesRefresher.setEnabled(( i == 0 ));
	}

	@Override
	public void onResume() {
		super.onResume();

		populateRatesListView();
		( (MainActivity) getActivity() ).appBarLayout.addOnOffsetChangedListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		( (MainActivity) getActivity() ).appBarLayout.removeOnOffsetChangedListener(this);
	}

	private void populateRatesListView() {
//		srQuotesRefresher.setRefreshing(false);

		if( mRatesListView == null ){
			mRatesListView = (RecyclerView) getActivity().findViewById(R.id.stockRatesList);
			mRatesListView.setLayoutManager(new LinearLayoutManager(getActivity()));

			final SharedPreferences prefs = getActivity().getSharedPreferences(Settings.PREFS_NAME,
					Context.MODE_PRIVATE);

			List<String> ratesList;
			try{
				ratesList = Arrays.asList(prefs.getString(Settings.Display.ratesList,
						Settings.Display.ratesListDefault).split("\\s*,\\s*"));
			} catch( NullPointerException ignored ){
				return;
			}

			JSONObject ratesJson;
			try{
				ratesJson = new JSONObject(prefs.getString(Settings.Rates.ratesKey, ""));
			} catch( JSONException ignored ){
				return;
			}

			final ExRatesGroup exRatesGroup = new ExRatesGroup("", ratesList, ratesJson);

			if( mStockItemsAdapter == null ){
				mStockItemsAdapter = new StockItemsAdapter(getActivity(),
						fromExRates(exRatesGroup.exRates), this);
			} else {
				mStockItemsAdapter.setStockItems(fromExRates(exRatesGroup.exRates));
				mStockItemsAdapter.notifyDataSetChanged();
			}
			mRatesListView.setAdapter(mStockItemsAdapter);
		}
	}

	public List<StockItem> fromExRates(List<ExRate> exRates) {
		final ArrayList<StockItem> mStockItems = new ArrayList<>();
		final double mainValue = ( (MainActivity) getActivity() ).mainValue;

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

		return mStockItems;
	}

	public void showCalculatorDialog(int listItemPosition, String title, double value) {
		FragmentManager fm = getActivity().getSupportFragmentManager();

		CalculatorDialog calculatorDialog = new CalculatorDialog();
		calculatorDialog.setTargetFragment(this, 0);

		calculatorDialog.title = title;
		calculatorDialog.value = value;
		calculatorDialog.listItemPosition = listItemPosition;
		calculatorDialog.show(fm, CalculatorDialog.TAG_DIALOG);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if( resultCode == Activity.RESULT_OK ){
			final int listItemPosition = data.getIntExtra(CalculatorDialog.TAG_LIST_ITEM_POSITION, -1);
			final double value = data.getDoubleExtra(CalculatorDialog.TAG_VALUE, 0);

			final StockItem stockItem = mStockItemsAdapter.getItem(listItemPosition);

			mStockItemsAdapter.fillValuesFromMainValue(value * stockItem.lastPrice / stockItem.faceValue);
			stockItem.value = value;
			mStockItemsAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onRefresh() {
		// TODO: 15.09.2015 Make force update
		srQuotesRefresher.setRefreshing(false);
		Toast.makeText(getActivity(), R.string.force_update_na, Toast.LENGTH_SHORT).show();

		if( mRatesListView != null ) populateRatesListView();
	}
}
