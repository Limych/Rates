package ru.khrolenok.exchangerates;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by Limych on 15.08.2015.
 */
public class ExRate {

    public int rate_id;
    public String groupCode;
    public String goodCode;
    public String currencyCode;
    public int faceValue;
    public double initialBid;
    public double lastBid;
    public Date updateTS;
    public double value = 0;

    public boolean isCompactTitleMode;
    public boolean isShortFormat;

    public ExRateViewsBounds viewsBounds;

    public ExRate(JSONArray rateData) {
        viewsBounds = new ExRateViewsBounds();

        try {
            groupCode = rateData.getString(Settings.Rates.Columns.GROUP);
            goodCode = rateData.getString(Settings.Rates.Columns.GOOD);
            currencyCode = rateData.getString(Settings.Rates.Columns.CURRENCY);
            faceValue = rateData.getInt(Settings.Rates.Columns.FACE_VALUE);
            initialBid = rateData.getDouble(Settings.Rates.Columns.INITIAL_BID);
            lastBid = rateData.getDouble(Settings.Rates.Columns.LAST_BID);
            updateTS = new Date(rateData.getLong(Settings.Rates.Columns.TIMESTAMP) * 1000);
        } catch (JSONException ignored) {
        }

        rate_id = (groupCode + goodCode + currencyCode).hashCode();
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

    public String getTitle() {
        String title = this.goodCode;
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

    public RemoteViews buildWidgetViews(Context context, boolean isShowChange, boolean invertColors){
        int color = R.color.change_none;
        final int colorUp = ( !invertColors ? R.color.change_green : R.color.change_red);
        final int colorDown = ( !invertColors ? R.color.change_red : R.color.change_green );

        RemoteViews ratesItem = new RemoteViews(context.getPackageName(), R.layout.widget_layout_item);

        ratesItem.setInt(R.id.itemSymbol, "setMinWidth", viewsBounds.titleWidth);
        ratesItem.setInt(R.id.itemPrice, "setMinWidth", viewsBounds.bidWidth);

        int[] textSizeAttr = new int[] { android.R.attr.textSize };
        TypedArray a = context.obtainStyledAttributes(R.style.AppWidget, textSizeAttr);
        final int textSize = a.getDimensionPixelSize(0, -1);
        a.recycle();
        if(textSize > 0) {
            ratesItem.setInt(R.id.itemChangeDirection, "setMinWidth", textSize);
        }

        ratesItem.setInt(R.id.itemChange, "setMinWidth", (isShowChange
                ? viewsBounds.changeWidth
                : 0));

        ratesItem.setTextViewText(R.id.itemSymbol, getTitle());

        if( isEmpty() ){
            ratesItem.setTextViewText(R.id.itemPrice, context.getString(R.string.rate_none));
            ratesItem.setTextViewText(R.id.itemChangeDirection, context.getString(R.string.change_direction_none));
            ratesItem.setTextViewText(R.id.itemChange, "");
        }else{
            ratesItem.setTextViewText(R.id.itemPrice, getLastBidFormatted());
            if( hasBidChange() ){
                ratesItem.setTextViewText(R.id.itemChangeDirection, context.getString(R.string.change_direction_none));
            }else if( bidChange() > 0 ) {
                ratesItem.setTextViewText(R.id.itemChangeDirection, context.getString(R.string.change_direction_up));
                color = colorUp;
            }else{
                ratesItem.setTextViewText(R.id.itemChangeDirection, context.getString(R.string.change_direction_down));
                color = colorDown;
            }
            ratesItem.setTextViewText(R.id.itemChange, ( isShowChange ? getChangeFormatted() : "" ));
        }
        ratesItem.setTextColor(R.id.itemPrice, context.getResources().getColor(color));
        ratesItem.setTextColor(R.id.itemChangeDirection, context.getResources().getColor(color));
        ratesItem.setTextColor(R.id.itemChange, context.getResources().getColor(color));

        return ratesItem;
    }

    static String format(double rate){
        if( rate == 0 ){
            return "0";
        } else {
            final DecimalFormat df = new DecimalFormat("0.00");

            return df.format(rate);
        }
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

