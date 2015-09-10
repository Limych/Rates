package ru.khrolenok.exchangerates;

import android.app.Application;
import android.content.res.Configuration;

import ru.khrolenok.exchangerates.util.StockNames;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Limych on 05.09.2015.
 */
public class ExRatesApplication extends Application {

	private static final String FONT_PATH = "fonts/Roboto-Regular.ttf";

	@Override
	public void onCreate() {
		super.onCreate();

		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
						.setDefaultFontPath(FONT_PATH)
						.setFontAttrId(R.attr.fontPath)
						.build()
		);
		StockNames.getInstance().init(getApplicationContext());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		StockNames.getInstance().init(getApplicationContext());
	}
}
