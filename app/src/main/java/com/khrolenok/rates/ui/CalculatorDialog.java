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

package com.khrolenok.rates.ui;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.khrolenok.rates.R;
import com.khrolenok.rates.util.EvaluateString;

import java.text.DecimalFormat;

/**
 * Created by Limych on 05.09.2015
 */
public class CalculatorDialog extends DialogFragment implements View.OnClickListener, View.OnLongClickListener {

	public static final String TAG_DIALOG = "Calculator";

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

		final ViewGroup pad = (ViewGroup) view.findViewById(R.id.pad);
		for( int childIndex = pad.getChildCount() - 1; childIndex >= 0; --childIndex ) {
			final View v = pad.getChildAt(childIndex);
			if( v instanceof Button || v instanceof ImageButton ){
				v.setOnClickListener(this);
			}
		}

		view.findViewById(R.id.del).setOnLongClickListener(this);

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
			case R.id.paren:
				int cntLParen = 0;
				int cntRParen = 0;
				for( int i = 0; i < expr.length(); i++ ) {
					if( expr.charAt(i) == '(' ) cntLParen++;
					else if( expr.charAt(i) == ')' ) cntRParen++;
				}
				expr += ( cntLParen != cntRParen && expr.matches(".*[0-9.,%)]") ? ")"
						: ( expr.matches(".*[+\\-*/−×÷(]") ? "(" : "×(" ) );
				break;
			case R.id.del:
				if( !expr.isEmpty() ){
					expr = expr.substring(0, expr.length() - 1);
					if( expr.isEmpty() ) expr = "0";
				}
				break;
			case R.id.set:
				dismiss();
				Intent intent = new Intent();
				intent.putExtra(TAG_LIST_ITEM_POSITION, this.listItemPosition);
				intent.putExtra(TAG_VALUE, this.value);
				getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
				return;
			case R.id.dec_point:
				expr += ( expr.matches(".*[)%]") ? "×" : "" ) + ( expr.matches(".*\\.[0-9]*") ? ""
						: ( expr.matches(".*[0-9]") ? "" : "0" ) + ( (Button) v ).getText() );
				break;
			case R.id.op_add:
			case R.id.op_sub:
			case R.id.op_mul:
			case R.id.op_div:
				if( expr.matches(".*[+\\-*/−×÷]") ){
					expr = expr.substring(0, expr.length() - 1);
				}
				expr += (String) ( (Button) v ).getText();
				break;
			case R.id.op_perc:
				if( expr.matches(".*[+\\-*/−×÷].*") && expr.matches(".*[0-9.,)]") ){
					expr += (String) ( (Button) v ).getText();
				}
				break;

			default:
				expr += ( expr.matches(".*[)%]") ? "×" : "" ) + ( (Button) v ).getText();
				break;
		}
		expr = expr.replaceFirst("(^|.*[+\\-*/−×÷(])0+([0-9])", "$1$2");
		expressionTextView.setText(expr);
		try{
			setResult(EvaluateString.evaluate(expr, false));
		} catch( Exception ignored ){
			showError(mContext.getString(R.string.error_in_expression));
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if( v.getId() == R.id.del ){
			expressionTextView.setText("0");
		}
		return false;
	}

	public static Spanned formatValue(double value) {
		final DecimalFormat df = new DecimalFormat();

		final long integerPart = (long) value;
		final double fractionalPart = Math.abs(value - integerPart);

		final String integerFormat = ( Math.abs(integerPart) <= 9999 ? "###0" : "#,##0" );
		df.applyPattern(integerFormat + ";−" + integerFormat);
		final String integerStr = df.format(integerPart);

		final String fractionalFormat = "0.0000";
		df.applyPattern(fractionalFormat.substring(0, 2 + 2));
		final String fractionalStr = df.format(fractionalPart).substring(1);

		return Html.fromHtml(integerStr + "<small>" + fractionalStr + "</small>");
	}
}
