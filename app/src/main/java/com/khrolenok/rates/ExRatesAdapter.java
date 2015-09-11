//package com.khrolenok.rates;
//
//import android.content.Context;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import java.text.NumberFormat;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.List;
//
//import com.khrolenok.rates.view.controller.MainActivity;
//
///**
// * Created by Limych on 01.09.2015.
// */
//class ExRatesAdapter extends BaseAdapter {
//    private final Context mContext;
//    public List<ExRate> exRates;
//
//    public ExRatesAdapter(Context mContext) {
//        this.mContext = mContext;
//        exRates = new ArrayList<ExRate>();
//    }
//
//    public ExRatesAdapter(Context mContext, List<ExRate> exRates) {
//        this.mContext = mContext;
//        this.exRates = exRates;
//    }
//
//    public void add(ExRate exRate){
//        if( exRate != null ) {
//            exRates.add(exRate);
//        }
//    }
//
//    @Override
//    public int getCount() {
//        return exRates.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return exRates.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return exRates.get(position).rate_id;
//    }
//
//    public String getGroupTitle(String groupCode){
//        switch (groupCode){
//            case Settings.Rates.Groups.OFFICIAL:
//                return mContext.getString(R.string.title_official);
//            case Settings.Rates.Groups.FOREX:
//                return mContext.getString(R.string.title_forex);
//            case Settings.Rates.Groups.STOCK:
//                return mContext.getString(R.string.title_stock);
//
//            default:
//                return groupCode;
//        }
//    }
//
//    @Override
//    public View getView(final int position, View convertView, ViewGroup parent) {
//        View rowView = convertView;
//
//        if( rowView == null ) {
//            final LayoutInflater inflater = (LayoutInflater) mContext
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            rowView = inflater.inflate(R.layout.activity_main_item, parent, false);
//        }
//
//        if( position % 2 == 0 ) {
//            rowView.setBackgroundColor(mContext.getResources().getColor(R.color.list_background_even));
//        }
//
//        final TextView titleView = (TextView) rowView.findViewById(R.id.item_title);
//        final TextView groupView = (TextView) rowView.findViewById(R.id.item_group);
//        final TextView rateView = (TextView) rowView.findViewById(R.id.item_price);
//        final EditText valueView = (EditText) rowView.findViewById(R.id.item_value);
//
//        final ExRate rate = exRates.get(position);
//
//        titleView.setText(rate.goodCode);
//        groupView.setText(getGroupTitle(rate.groupCode));
//        rateView.setText(rate.getLastBidFormatted());
//
//        valueView.setTag(rate);
//        final double value = ((MainActivity) mContext).mainValue * rate.faceValue / rate.lastBid;
//        valueView.setText(ExRate.format(value));
//
//        valueView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                ((MainActivity) mContext).focus = (hasFocus ? v : null);
//            }
//        });
//
//        //every time the user adds/removes a character from the edit text, save
//        //the current value of the edit text to retrieve later
//        valueView.addTextChangedListener(new myTextWatcher(rowView));
//
//        return rowView;
//    }
//
//    private class myTextWatcher implements TextWatcher {
//        View view;
//
//        public myTextWatcher(View view) {
//            this.view = view;
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            // do nothing
//        }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            // do nothing
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//            double value = 0;
//            final double mainValue;
//            final EditText valueView = (EditText) view.findViewById(R.id.item_value);
//
//            if( valueView != ((MainActivity) mContext).focus ){
//                return;
//            }
//
//            final ExRate rate = (ExRate) valueView.getTag();
//
//            try {
//                value = NumberFormat.getInstance().parse(s.toString()).doubleValue();
//            } catch (ParseException ignored) {
//            }
//
//            if( rate.value != value ) {
//                rate.value = value;
//
//                mainValue = value * rate.lastBid / rate.faceValue;
//                ((MainActivity) mContext).setMainValue(mainValue);
//
//                ListView list = (ListView) ((MainActivity) mContext).findViewById(R.id.rates_list);
//                final int start = list.getFirstVisiblePosition();
//                for (int i = list.getLastVisiblePosition(); i >= start; i--) {
//                    final View v = list.getChildAt(i - start);
//                    if( v != view ){
//                        final EditText v_valueView = (EditText) v.findViewById(R.id.item_value);
//                        final ExRate v_rate = (ExRate) v_valueView.getTag();
//                        final double v_value = mainValue * v_rate.faceValue / v_rate.lastBid;
//                        if( v_rate.value != v_value ){
//                            v_rate.value = v_value;
//                            v_valueView.setText(ExRate.format(v_value));
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//}
