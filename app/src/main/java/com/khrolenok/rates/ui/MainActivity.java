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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.khrolenok.rates.BuildConfig;
import com.khrolenok.rates.ExRatesApplication;
import com.khrolenok.rates.R;
import com.khrolenok.rates.Settings;
import com.khrolenok.rates.util.AppStore;

import java.util.ArrayList;
import java.util.List;

import trikita.log.Log;

public class MainActivity extends AppCompatActivity {

	private static final String STATE_MAIN_VALUE_KEY = "MainActivity$MainValue";

	public AppBarLayout appBarLayout;

	public double mainValue = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Check whether we're recreating a previously destroyed instance
		if( savedInstanceState != null ){
			// Restore value of members from saved state
			mainValue = savedInstanceState.getDouble(STATE_MAIN_VALUE_KEY);
		}

		appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);

		initToolbar();
		initViewPagerAndTabs();
		initAdView();

		switch( ExRatesApplication.mode ){
			case ExRatesApplication.MODE_ABOUT:
				DialogsManager.showAboutDialog(this);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_main, menu);

		if( AppStore.detect(this) != AppStore.UNKNOWN ){
			menu.findItem(R.id.action_appstore).setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch( id ){
			case R.id.action_appstore:
				// Track event
				ExRatesApplication.getInstance().trackEvent("AppStore", "Open");

				AppStore.openStore(this, AppStore.GOOGLE_PLAY);
				return true;
			case R.id.action_preferences:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.action_idea_suggest:
				// Track event
				ExRatesApplication.getInstance().trackEvent("IdeaSuggest", "Open");

				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(Settings.IDEA_SUGGEST_URL));
				startActivity(i);
				break;
			case R.id.action_about:
				DialogsManager.showAboutDialog(this);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Save the activity current state
		outState.putDouble(STATE_MAIN_VALUE_KEY, mainValue);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(outState);
	}

	private void initToolbar() {
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		setTitle(getString(R.string.app_name));
	}

	private void initViewPagerAndTabs() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.contentFrame);
		PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
		pagerAdapter.addFragment(new RatesFragment(), getString(R.string.tab_rates));
		viewPager.setAdapter(pagerAdapter);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
		if( pagerAdapter.getCount() <= 1 ){
			tabLayout.setVisibility(View.GONE);
		} else {
			tabLayout.setupWithViewPager(viewPager);
		}
	}

	private void initAdView() {
		if( !ExRatesApplication.isShowAds ){
			if( BuildConfig.DEBUG ) Log.v("Ads removed");
			findViewById(R.id.adView).setVisibility(View.GONE);

		} else {
			// Start AdMob
			final AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
					.build();
			final AdView mAdView = (AdView) findViewById(R.id.adView);
			mAdView.loadAd(adRequest);
		}
	}

	static class PagerAdapter extends FragmentPagerAdapter {

		private final List<Fragment> fragmentList = new ArrayList<>();
		private final List<String> fragmentTitleList = new ArrayList<>();

		public PagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		public void addFragment(Fragment fragment, String title) {
			fragmentList.add(fragment);
			fragmentTitleList.add(title);
		}

		@Override
		public Fragment getItem(int position) {
			return fragmentList.get(position);
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return fragmentTitleList.get(position);
		}
	}
}
