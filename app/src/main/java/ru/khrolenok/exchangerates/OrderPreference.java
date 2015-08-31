//package ru.khrolenok.exchangerates;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.os.Build;
//import android.preference.DialogPreference;
//import android.util.AttributeSet;
//
///**
// * Created by Limych on 22.07.2015.
// */
//public class OrderPreference extends DialogPreference {
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public OrderPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//
//        setDialogLayoutResource(R.layout.orderpreference_dialoglayout);
//    }
//
//    public OrderPreference(Context context, AttributeSet attrs, int defStyleAttr) {
//        this(context, attrs, defStyleAttr, 0);
//    }
//
//    public OrderPreference(Context context, AttributeSet attrs) {
//        this(context, attrs, android.R.attr.dialogPreferenceStyle);
//    }
//
//    public OrderPreference(Context context) {
//        this(context, null);
//    }
//
//    @Override
//    public CharSequence getSummary() {
//        return super.getSummary();
//    }
//
//}
