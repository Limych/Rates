package ru.khrolenok.exchangerates.model;

import android.text.Html;
import android.text.Spanned;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

import ru.khrolenok.exchangerates.Settings;

/**
 * Created by Limych on 03.09.2015.
 */
public class StockItem implements Comparable<StockItem>, Serializable {

	public String code;
	public String stockExchange;
	public String symbol;
	public String name;
	public int faceValue;
	public String priceCurrency;
	public double initialPrice;
	public double lastPrice;
	public double highPrice;
	public double lowPrice;
	public Date lastUpdate;
	public double value;

	public double getPriceChange() {
		return lastPrice - initialPrice;
	}

	public boolean hasPriceChange() {
		return Math.abs(getPriceChange() * 100 / initialPrice) >= Settings.Display.changesThreshold;
	}

	public static Spanned formatValue(double value, boolean isShowSign, boolean isShortFormat,
	                              boolean isFixDigits) {
		int digits = ( isShortFormat ? 2 : 4 );

		if( isFixDigits ){
			double tmp = value;
			while( digits > 0 && Math.abs(tmp) > 99 ){
				tmp /= 10;
				digits--;
			}
		}

		final DecimalFormat df = new DecimalFormat();

		final long integerPart = (long) value;
		final double fractionalPart = Math.abs(value - integerPart);

		final String signFormat = ( isShowSign ? "+" : "" );
		final String integerFormat = ( Math.abs(integerPart) <= 9999 ? "###0" : "#,##0" );
		df.applyPattern(signFormat + integerFormat + ";−"+integerFormat);
		final String integerStr = df.format(integerPart);

		final String fractionalFormat = "0.0000";
		df.applyPattern(fractionalFormat.substring(0, 2 + digits));
		final String fractionalStr = df.format(fractionalPart).substring(1);

		return Html.fromHtml(integerStr + "<small>" + fractionalStr + "</small>");
	}

	public Spanned getLastPriceFormatted(boolean isShortFormat) {
		return formatValue(lastPrice, false, isShortFormat, true);
	}

	public Spanned getLastPriceFormatted() {
		return getLastPriceFormatted(false);
	}

	public Spanned getValueFormatted() {
		return formatValue(value, false, true, false);
	}

	public Spanned getPriceChangeFormatted(boolean isShortFormat) {
		return formatValue(getPriceChange(), true, isShortFormat, false);
	}

	public Spanned getPriceChangeFormatted() {
		return getPriceChangeFormatted(false);
	}

	public int compareTo(StockItem item) {
		return 0;
	}

}
