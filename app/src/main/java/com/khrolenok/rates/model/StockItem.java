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

package com.khrolenok.rates.model;

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;

import com.khrolenok.rates.Settings;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by Limych on 03.09.2015
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

	public int compareTo(@NonNull StockItem item) {
		return 0;
	}

}
