package ru.khrolenok.exchangerates.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ru.khrolenok.exchangerates.R;

/**
 * Created by Limych on 03.09.2015.
 */
public class SettingsActivity extends ActionBarActivity {

	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);

		if( savedInstanceState == null ){
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SettingsFragment())
					.commit();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_up));
		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent upIntent = NavUtils.getParentActivityIntent(SettingsActivity.this);
				navigateUpTo(upIntent);
			}
		});
	}

	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
		}

	}

}
