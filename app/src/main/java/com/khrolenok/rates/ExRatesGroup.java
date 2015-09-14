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

package com.khrolenok.rates;

import android.content.Context;
import android.content.res.TypedArray;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExRatesGroup {
    public String caption;
    public List<ExRate> exRates;

    public boolean isCompactTitleMode;
    public boolean isLongFormat;

    public ExRateViewsBounds viewsBounds;

    public ExRatesGroup(String caption) {
        this.caption = caption;
        viewsBounds = new ExRateViewsBounds();
        exRates = new ArrayList<>();
    }

    public ExRatesGroup(String caption, List<String> ratesList, JSONObject ratesJson) {
        this.caption = caption;
        viewsBounds = new ExRateViewsBounds();
        exRates = new ArrayList<>();

        for (String rate : ratesList) {
            JSONArray ratesArray;
            try {
                ratesArray = ratesJson.getJSONArray(rate);
            } catch (JSONException e) {
                continue;
            }

            add(new ExRate(ratesArray));
        }
    }

    public ExRatesGroup(String caption, String ratesGroup, List<String> ratesList, JSONObject ratesJson) {
        this.caption = caption;
        viewsBounds = new ExRateViewsBounds();
        exRates = new ArrayList<>();

        for (String rate : ratesList) {
            JSONArray ratesArray;
            try {
                ratesArray = ratesJson.getJSONArray(rate);
            } catch (JSONException e) {
                continue;
            }
            try {
                if (!ratesArray.getString(Settings.Rates.Columns.GROUP).equals(ratesGroup)) {
                    continue;
                }
            } catch (JSONException e) {
                continue;
            }

            add(new ExRate(ratesArray));
        }
    }

    public void add(ExRate exRate){
        if( exRate != null ) {
            viewsBounds.updateMaxBounds(exRate.viewsBounds);
            exRate.viewsBounds = viewsBounds;
            exRates.add(exRate);
        }
    }

    public Date getUpdateTS(){
        Date updateTS = null;
        for (ExRate exRate : exRates) {
            final Date rateTS = exRate.getUpdateTS();
            updateTS = ( updateTS == null || rateTS.after(updateTS)
                    ? rateTS
                    : updateTS );
        }
        return updateTS;
    }

    public ExRateViewsBounds calcViewsBounds(int mainFontSize, int changeFontSize){
//        if( BuildConfig.DEBUG ) Log.v(Settings.TAG, "ExRatesGroup.calcViewsBounds(" + mainFontSize + ", " + changeFontSize + ")");

        for (ExRate exRate : exRates) {
            exRate.isCompactTitleMode = isCompactTitleMode;
            exRate.isLongFormat = isLongFormat;

            exRate.calcViewsBounds(mainFontSize, changeFontSize);
            viewsBounds.updateMaxBounds(exRate, mainFontSize, changeFontSize);
        }

        return viewsBounds;
    }

    public ExRateViewsBounds calcViewsBounds(Context context){
        int mainFontSize, changeFontSize;
        final int[] textSizeAttr = new int[] { android.R.attr.textSize };
        TypedArray a = context.obtainStyledAttributes(R.style.AppWidget_Body, textSizeAttr);
        mainFontSize = a.getDimensionPixelSize(0, -1);
        a.recycle();
        a = context.obtainStyledAttributes(R.style.AppWidget_Small, textSizeAttr);
        changeFontSize = a.getDimensionPixelSize(0, -1);
        a.recycle();
        final float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        if( mainFontSize <= 0 ) {
            mainFontSize = (int) (14 * scaledDensity);
        }
        if( changeFontSize <= 0 ) {
            changeFontSize = (int) (9 * scaledDensity);
        }

        return calcViewsBounds(mainFontSize, changeFontSize);
    }

    public RemoteViews buildWidgetViews(Context context, boolean isShowChange, boolean invertColors){
//        if( BuildConfig.DEBUG ) Log.v(Settings.TAG, "ExRatesGroup.buildWidgetViews(…)");

        RemoteViews ratesGroup = new RemoteViews(context.getPackageName(), R.layout.widget_layout_group);
        ratesGroup.removeAllViews(R.id.table_rates);
        ratesGroup.setTextViewText(R.id.group_caption, caption);

        for (ExRate exRate : exRates) {
            exRate.isCompactTitleMode = isCompactTitleMode;
            exRate.isLongFormat = isLongFormat;

            ratesGroup.addView(R.id.table_rates,
                    exRate.buildWidgetViews(context, isShowChange, invertColors));
        }

        return ratesGroup;
    }
}
