package ru.khrolenok.exchangerates.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import ru.khrolenok.exchangerates.R;

public class MainActivity extends ActionBarActivity {

	private static final String TOOLBAR_TEXTVIEW_FIELD_NAME = "mTitleTextView";
	private static final String TOOLBAR_NAV_BTN_FIELD_NAME = "mNavButtonView";

	private Toolbar mToolbar;
//	private TextView mToolbarTitle;
//	private ImageButton mToolbarButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
//
//		try{
//			Field f = mToolbar.getClass().getDeclaredField(TOOLBAR_TEXTVIEW_FIELD_NAME);
//			f.setAccessible(true);
//			mToolbarTitle = (TextView) f.get(mToolbar);
//
//			f = mToolbar.getClass().getDeclaredField(TOOLBAR_NAV_BTN_FIELD_NAME);
//			f.setAccessible(true);
//			mToolbarButton = (ImageButton) f.get(mToolbar);
//
//		} catch( NoSuchFieldException e ){
//			e.printStackTrace();
//		} catch( IllegalAccessException e ){
//			e.printStackTrace();
//		}

		// Init content frame by default
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, new MainFragment()).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_main, menu);

		mToolbar.setLogo(null);
		mToolbar.setTitle(getString(R.string.app_name));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// noinspection SimplifiableIfStatement
		if( id == R.id.action_preferences ){
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
