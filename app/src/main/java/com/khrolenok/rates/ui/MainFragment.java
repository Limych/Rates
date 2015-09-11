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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.khrolenok.rates.ExRatesGroup;
import com.khrolenok.rates.R;
import com.khrolenok.rates.Settings;
import com.khrolenok.rates.controller.StockItemsAdapter;
import com.khrolenok.rates.model.StockItem;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Limych on 07.09.2015
 */
public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

	private DynamicListView mMainListView;

	private StockItemsAdapter mStockItemsAdapter;
	private SwipeRefreshLayout srQuotesRefresher;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		srQuotesRefresher = (SwipeRefreshLayout) rootView.findViewById(R.id.srQuotesRefresher);
		srQuotesRefresher.setOnRefreshListener(this);

		// AdMob
		final AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
				.build();
		final AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
		mAdView.loadAd(adRequest);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		populateMainListView();
	}

	private void populateMainListView() {
		srQuotesRefresher.setRefreshing(false);

		if( mMainListView == null ){
			mMainListView = (DynamicListView) getActivity().findViewById(R.id.mStockItemsList);
//			mMainListView.enableDragAndDrop();

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
						new ArrayList<StockItem>(), this);
				mStockItemsAdapter.fillFromExRates(exRatesGroup.exRates);
			} else {
				mStockItemsAdapter.clear();
				mStockItemsAdapter.fillFromExRates(exRatesGroup.exRates);
				mStockItemsAdapter.notifyDataSetChanged();
			}
			mMainListView.setAdapter(mStockItemsAdapter);
		}
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
		if( mMainListView != null ) populateMainListView();
	}
}
