/*
 * Copyright (c) 2015. Andrey “Limych” Khrolenok
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

package ru.khrolenok.exchangerates.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import ru.khrolenok.exchangerates.R;
import ru.khrolenok.exchangerates.util.EvaluateString;

/**
 * Created by Limych on 05.09.2015.
 */
public class CalculatorDialog extends DialogFragment implements View.OnClickListener, View.OnLongClickListener {

	public static final String TAG_LIST_ITEM_POSITION = "ListItemPosition";
	public static final String TAG_VALUE = "Value";

	public double value = 0;
	public int listItemPosition = -1;
	public String title;

	private Context mContext;
	public TextView resultTextView;

	public TextView expressionTextView;

	public CalculatorDialog() { /* none */ }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = inflater.getContext();

		View view = inflater.inflate(R.layout.dialog_calculator, container);

		expressionTextView = (TextView) view.findViewById(R.id.expressionTextView);
		resultTextView = (TextView) view.findViewById(R.id.resultTextView);
		final DecimalFormat df = new DecimalFormat("0.##");

		( (TextView) view.findViewById(R.id.title) ).setText(title);
		expressionTextView.setText(df.format(value));
		setResult(value);

		view.findViewById(R.id.button0).setOnClickListener(this);
		view.findViewById(R.id.button00).setOnClickListener(this);
		view.findViewById(R.id.button1).setOnClickListener(this);
		view.findViewById(R.id.button2).setOnClickListener(this);
		view.findViewById(R.id.button3).setOnClickListener(this);
		view.findViewById(R.id.button4).setOnClickListener(this);
		view.findViewById(R.id.button5).setOnClickListener(this);
		view.findViewById(R.id.button6).setOnClickListener(this);
		view.findViewById(R.id.button7).setOnClickListener(this);
		view.findViewById(R.id.button8).setOnClickListener(this);
		view.findViewById(R.id.button9).setOnClickListener(this);
		view.findViewById(R.id.buttonBrk).setOnClickListener(this);
		view.findViewById(R.id.buttonBS).setOnClickListener(this);
		view.findViewById(R.id.buttonDiv).setOnClickListener(this);
		view.findViewById(R.id.buttonDot).setOnClickListener(this);
		view.findViewById(R.id.buttonMinus).setOnClickListener(this);
		view.findViewById(R.id.buttonMult).setOnClickListener(this);
		view.findViewById(R.id.buttonPerc).setOnClickListener(this);
		view.findViewById(R.id.buttonPlus).setOnClickListener(this);
		view.findViewById(R.id.buttonOk).setOnClickListener(this);

		view.findViewById(R.id.buttonBS).setOnLongClickListener(this);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return view;
	}

	private void setResult(double value) {
		this.value = value;
		resultTextView.setText("= " + formatValue(value));
		resultTextView.setTextColor(mContext.getResources().getColor(R.color.secondary_text));
	}

	private void showError(String msg) {
		resultTextView.setText(msg);
		resultTextView.setTextColor(mContext.getResources().getColor(R.color.material_red_500));
	}

	@Override
	public void onClick(View v) {
		String expr = (String) expressionTextView.getText();
		switch( v.getId() ){
			case R.id.button0:
				expr += "0";
				break;
			case R.id.button00:
				expr += "00";
				break;
			case R.id.button1:
				expr += "1";
				break;
			case R.id.button2:
				expr += "2";
				break;
			case R.id.button3:
				expr += "3";
				break;
			case R.id.button4:
				expr += "4";
				break;
			case R.id.button5:
				expr += "5";
				break;
			case R.id.button6:
				expr += "6";
				break;
			case R.id.button7:
				expr += "7";
				break;
			case R.id.button8:
				expr += "8";
				break;
			case R.id.button9:
				expr += "9";
				break;

			case R.id.buttonPlus:
				expr += "+";
				break;
			case R.id.buttonMinus:
				expr += "-";
				break;
			case R.id.buttonMult:
				expr += "×";
				break;
			case R.id.buttonDiv:
				expr += "/";
				break;
			case R.id.buttonPerc:
				expr += "%";
				break;

			case R.id.buttonDot:
				char decSeparator = '.';
				final NumberFormat nf = NumberFormat.getInstance();
				if( nf instanceof DecimalFormat ){
					DecimalFormatSymbols sym = ( (DecimalFormat) nf ).getDecimalFormatSymbols();
					decSeparator = sym.getDecimalSeparator();
				}
				expr += decSeparator;
				break;

			case R.id.buttonBrk:
				expr += ( expr.matches(".*[0-9.%)]") ? ")" : "(" );
				break;

			case R.id.buttonBS:
				if( !expr.isEmpty() ){
					expr = expr.substring(0, expr.length() - 1);
				}
				break;

			case R.id.buttonOk:
				dismiss();
				Intent intent = new Intent();
				intent.putExtra(TAG_LIST_ITEM_POSITION, this.listItemPosition);
				intent.putExtra(TAG_VALUE, this.value);
				getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
				return;
		}
		expressionTextView.setText(expr);
		try{
			setResult(EvaluateString.evaluate(expr, false));
		} catch( Exception ignored ){
			showError(mContext.getString(R.string.error_in_expression));
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if( v.getId() == R.id.buttonBS ){
			expressionTextView.setText("");
		}
		return false;
	}

	public static Spanned formatValue(double value) {
		final DecimalFormat df = new DecimalFormat();

		final long integerPart = (long) value;
		final double fractionalPart = Math.abs(value - integerPart);

		final String integerFormat = ( Math.abs(integerPart) <= 9999 ? "###0" : "#,##0" );
		df.applyPattern(integerFormat + ";−"+integerFormat);
		final String integerStr = df.format(integerPart);

		final String fractionalFormat = "0.0000";
		df.applyPattern(fractionalFormat.substring(0, 2 + 2));
		final String fractionalStr = df.format(fractionalPart).substring(1);

		return Html.fromHtml(integerStr + "<small>" + fractionalStr + "</small>");
	}
}
