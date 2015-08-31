package ru.khrolenok.exchangerates;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Limych on 15.08.2015.
 */
public class ExRate {

    public String rateCode;
    public double initialBid;
    public double lastBid;
    public Date updateTS;

    public boolean isCompactTitleMode;
    public boolean isShortFormat;

    public ExRateViewsBounds viewsBounds;

    public ExRate(String rateCode) {
        this.rateCode = rateCode;
        viewsBounds = new ExRateViewsBounds();
    }

    public ExRate(String rateCode, JSONArray rateData) {
        this.rateCode = rateCode;
        viewsBounds = new ExRateViewsBounds();

        try {
            initialBid = rateData.getDouble(Settings.Rates.Columns.INITIAL_BID);
            lastBid = rateData.getDouble(Settings.Rates.Columns.LAST_BID);
            updateTS = new Date(rateData.getLong(Settings.Rates.Columns.TIMESTAMP));
        } catch (JSONException ignored) {
        }
    }

    public boolean isEmpty(){
        return initialBid == 0 && lastBid == 0;
    }

    public double bidChange(){
        return lastBid - initialBid;
    }

    public boolean hasBidChange(){
        return hasBidChange(Settings.Display.changesThreshold);
    }

    public boolean hasBidChange(double changesThreshold){
        return Math.abs(bidChange() * 100 / initialBid) < changesThreshold;
    }

    public boolean hasBidGrowth(){
        return bidChange() > 0;
    }

    public String getTitle() {
        String title = this.rateCode.substring(0, 3);
        if( isCompactTitleMode ){
            switch (title){
                case "USD": title = "$"; break;
                case "EUR": title = "€"; break;
                case "BYR": title = "Br"; break;
                case "GBP": title = "£"; break;

                case "CNY": title = "¥"; break;
                case "JPY": title = "¥"; break;

                case "RUR":
                case "RUB": title = "\u20BD"; break;
            }
        }
        return title;
    }

    public Date getUpdateTS() {
        return updateTS;
    }

    protected String formatValue(double value, boolean isShowSign, boolean isShortFormat) {
        int digits = ( isShortFormat ? 2 : 4 );
        double tmp = value;

        while( digits > 0  && Math.abs(tmp) > 99 ){
            tmp /= 10;
            digits--;
        }
        return String.format("%" + (isShowSign ? "+" : "") + "." + digits + "f", value)
                .replace('-', '−');
    }

    public String getLastBidFormatted() {
        return formatValue(lastBid, false, isShortFormat);
    }

    public String getChangeFormatted() {
        return formatValue(bidChange(), true, isShortFormat);
    }

    protected void getTextBounds(String text, int fontSize, Rect bounds) {
        Paint p = new Paint();
        p.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        p.setTextSize(fontSize);
        p.setAntiAlias(true);

        p.getTextBounds(text, 0, text.length(), bounds);
        bounds.right += bounds.left;
        bounds.left = 0;
        bounds.bottom += bounds.top;
        bounds.top = 0;
    }

    public Rect getBounds(int mainFontSize){
        return getBounds(mainFontSize, mainFontSize);
    }

    public Rect getBounds(int mainFontSize, int changeFontSize){
        calcViewsBounds(mainFontSize, changeFontSize);

        return new Rect(0, 0, viewsBounds.getSummaryWidth(), viewsBounds.height);
    }

    public ExRateViewsBounds calcViewsBounds(int mainFontSize, int changeFontSize){
        Rect tb = new Rect();
        int height = 0;

        getTextBounds(getTitle(), mainFontSize, tb);
        int titleWidth = tb.width();
        height = Math.max(height, tb.height());

        getTextBounds(getLastBidFormatted(), mainFontSize, tb);
        int bidWidth = tb.width();
        height = Math.max(height, tb.height());

        getTextBounds(getChangeFormatted(), changeFontSize, tb);
        int changeWidth = tb.width();
        height = Math.max(height, tb.height());

        viewsBounds.updateMaxBounds(titleWidth, bidWidth, changeWidth, height);

        return viewsBounds;
    }

    public RemoteViews buildRemoteViews(Context context, boolean isShowChange, boolean invertColors){
        int color = R.color.change_none;
        final int colorUp = ( !invertColors ? R.color.change_green : R.color.change_red);
        final int colorDown = ( !invertColors ? R.color.change_red : R.color.change_green );

        RemoteViews ratesItem = new RemoteViews(context.getPackageName(), R.layout.widget_layout_item);

        ratesItem.setInt(R.id.currency_title, "setMinWidth", viewsBounds.titleWidth);
        ratesItem.setInt(R.id.currency_rate, "setMinWidth", viewsBounds.bidWidth);

        int[] textSizeAttr = new int[] { android.R.attr.textSize };
        TypedArray a = context.obtainStyledAttributes(R.style.AppWidget, textSizeAttr);
        final int textSize = a.getDimensionPixelSize(0, -1);
        a.recycle();
        if(textSize > 0) {
            ratesItem.setInt(R.id.currency_change_direction, "setMinWidth", textSize);
        }

        ratesItem.setInt(R.id.currency_change, "setMinWidth", ( isShowChange
                ? viewsBounds.changeWidth
                : 0 ));

        ratesItem.setTextViewText(R.id.currency_title, getTitle());

        if( isEmpty() ){
            ratesItem.setTextViewText(R.id.currency_rate, context.getString(R.string.rate_none));
            ratesItem.setTextViewText(R.id.currency_change_direction, context.getString(R.string.change_none));
            ratesItem.setTextViewText(R.id.currency_change, "");
        }else{
            ratesItem.setTextViewText(R.id.currency_rate, getLastBidFormatted());
            if( hasBidChange() ){
                ratesItem.setTextViewText(R.id.currency_change_direction, context.getString(R.string.change_none));
            }else if( hasBidGrowth() ) {
                ratesItem.setTextViewText(R.id.currency_change_direction, context.getString(R.string.change_up));
                color = colorUp;
            }else{
                ratesItem.setTextViewText(R.id.currency_change_direction, context.getString(R.string.change_down));
                color = colorDown;
            }
            ratesItem.setTextViewText(R.id.currency_change, ( isShowChange ? getChangeFormatted() : "" ));
        }
        ratesItem.setTextColor(R.id.currency_rate, context.getResources().getColor(color));
        ratesItem.setTextColor(R.id.currency_change_direction, context.getResources().getColor(color));
        ratesItem.setTextColor(R.id.currency_change, context.getResources().getColor(color));

        return ratesItem;
    }
}

class ExRateViewsBounds {
    public int titleWidth;
    public int bidWidth;
    public int changeWidth;

    public int height;

    public ExRateViewsBounds() {}

    public ExRateViewsBounds(int titleWidth, int bidWidth, int changeWidth, int height) {
        set(titleWidth, bidWidth, changeWidth, height);
    }

    public void set(ExRateViewsBounds viewsBounds){
        this.titleWidth = viewsBounds.titleWidth;
        this.bidWidth = viewsBounds.bidWidth;
        this.changeWidth = viewsBounds.changeWidth;
        this.height = viewsBounds.height;
    }

    public void set(int titleWidth, int bidWidth, int changeWidth, int height) {
        this.titleWidth = titleWidth;
        this.bidWidth = bidWidth;
        this.changeWidth = changeWidth;
        this.height = height;
    }

    public void updateMaxBounds(int titleWidth, int bidWidth, int changeWidth, int height){
        this.titleWidth = Math.max(this.titleWidth, titleWidth);
        this.bidWidth = Math.max(this.bidWidth, bidWidth);
        this.changeWidth = Math.max(this.changeWidth, changeWidth);
        this.height = Math.max(this.height, height);
    }

    public void updateMaxBounds(ExRateViewsBounds viewsBounds) {
        updateMaxBounds(viewsBounds.titleWidth, viewsBounds.bidWidth, viewsBounds.changeWidth,
                viewsBounds.height);
    }

    public void updateMaxBounds(ExRate exRate, int mainFontSize) {
        updateMaxBounds(exRate, mainFontSize, mainFontSize);
    }

    public void updateMaxBounds(ExRate exRate, int mainFontSize, int changeFontSize) {
        updateMaxBounds(exRate.calcViewsBounds(mainFontSize, changeFontSize));
    }

    public int getSummaryWidth(){
        return titleWidth + bidWidth + changeWidth;
    }
}

class ExRatesGroup {
    public String caption;
    public List<ExRate> exRates;

    public boolean isCompactTitleMode;
    public boolean isShortFormat;

    public ExRateViewsBounds viewsBounds;

    public ExRatesGroup(String caption) {
        this.caption = caption;
        viewsBounds = new ExRateViewsBounds();
        exRates = new ArrayList<ExRate>();
    }

    public ExRatesGroup(String caption, String ratesType, List<String> ratesList, JSONObject ratesJson) {
        this.caption = caption;
        viewsBounds = new ExRateViewsBounds();
        exRates = new ArrayList<ExRate>();

        for (String rate : ratesList) {
            JSONArray ratesArray = null;
            try {
                ratesArray = ratesJson.getJSONArray(rate);
            } catch (JSONException e) {
                continue;
            }
            try {
                if (!ratesArray.getString(Settings.Rates.Columns.CROSS_TYPE).equals(ratesType)) {
                    continue;
                }
            } catch (JSONException e) {
                continue;
            }

            add(new ExRate(rate, ratesArray));
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
//        Log.v(Settings.LOG_TAG, "ExRatesGroup.calcViewsBounds(" + mainFontSize + ", " + changeFontSize + ")");

        for (ExRate exRate : exRates) {
            exRate.isCompactTitleMode = isCompactTitleMode;
            exRate.isShortFormat = isShortFormat;

            exRate.calcViewsBounds(mainFontSize, changeFontSize);
            viewsBounds.updateMaxBounds(exRate, mainFontSize, changeFontSize);
        }

        return viewsBounds;
    }

    public ExRateViewsBounds calcViewsBounds(Context context){
        int mainFontSize, changeFontSize;
        final int[] textSizeAttr = new int[] { android.R.attr.textSize };
        TypedArray a = context.obtainStyledAttributes(R.style.AppWidget, textSizeAttr);
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

    public RemoteViews buildRemoteViews(Context context, boolean isShowChange, boolean invertColors){
        Log.v(Settings.LOG_TAG, "ExRatesGroup.buildRemoteViews(…)");

        RemoteViews ratesGroup = new RemoteViews(context.getPackageName(), R.layout.widget_layout_group);
        ratesGroup.removeAllViews(R.id.table_rates);
        ratesGroup.setTextViewText(R.id.group_caption, caption);

        for (ExRate exRate : exRates) {
            exRate.isCompactTitleMode = isCompactTitleMode;
            exRate.isShortFormat = isShortFormat;

            ratesGroup.addView(R.id.table_rates,
                    exRate.buildRemoteViews(context, isShowChange, invertColors));
        }

        return ratesGroup;
    }
}