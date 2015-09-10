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

package ru.khrolenok.exchangerates.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Created by Limych on 06.09.2015.
 */
public class EvaluateString {

	public static double evaluate(String expression) {
		return evaluate(expression, true);
	}

	public static double evaluate(String expression, boolean useStrictLogic) {
		if( expression.isEmpty() ){
			return 0;
		}

		// Stacks for values and operators
		final Stack<Value> values = new Stack<Value>();
		final Stack<Character> ops = new Stack<Character>();

		expression = expression.replaceAll("−", "-");
		expression = expression.replaceAll("×", "*");
		expression = expression.replaceAll("÷", "/");

		/** remove all spaces from the expression **/
		expression = expression.replaceAll("\\s+", "");

		if( !useStrictLogic ){
			expression = expression.replaceAll("[\\(*/+-]+$", "");
			expression = expression.replaceAll("[*/+-]+([\\)*/+-])", "$1");
		}

		/** split the expression to tokens **/
		final StringTokenizer tokens = new StringTokenizer(expression, "()*/+-%", true);

		// Determine decimal separator
		String decSeparator = ".";
		final NumberFormat nf = NumberFormat.getInstance();
		if( nf instanceof DecimalFormat ){
			DecimalFormatSymbols sym = ( (DecimalFormat) nf ).getDecimalFormatSymbols();
			decSeparator = "" + sym.getDecimalSeparator();
		}

		while( tokens.hasMoreTokens() ){
			String tkn = tokens.nextToken();

			// Current token is a number, push it to stack for numbers
			if( tkn.matches("[0-9.,]+") ){
				values.push(new Value(Value.Type.NUMBER,
						Double.parseDouble(tkn.replace(decSeparator, "."))));

				// Current token is an opening brace, push it to 'ops'
			} else if( tkn.equals("(") ){
				ops.push(tkn.charAt(0));

				// Closing brace encountered, solve entire brace
			} else if( tkn.equals(")") ){
				while( !ops.empty() && ops.peek() != '(' )
					applyOp(ops.pop(), values, useStrictLogic);
				if( !ops.empty() && useStrictLogic ) ops.pop();

				// Current token is an operator.
			} else {
				// While top of 'ops' has same or greater precedence to current
				// token, which is an operator. Apply operator on top of 'ops'
				// to top two elements in values stack
				while( !ops.empty() && hasPrecedence(tkn.charAt(0), ops.peek()) )
					applyOp(ops.pop(), values, useStrictLogic);

				// Push current token to 'ops'.
				ops.push(tkn.charAt(0));
			}
		}

		// Entire expression has been parsed at this point, apply remaining
		// ops to remaining values
		while( !ops.empty() )
			applyOp(ops.pop(), values, useStrictLogic);

		// Top of 'values' contains result, return it
		return values.pop().value;
	}

	protected static final Map<Character, Integer> OPERATOR_LEVELS = new HashMap<Character, Integer>() {{
		put('(', 0);
		put('+', 1);
		put('-', 1);
		put('*', 2);
		put('/', 2);
		put('%', 3);
		put(')', Integer.MAX_VALUE);
	}};

	// Returns true if 'op2' has higher or same precedence as 'op1',
	// otherwise returns false.
	protected static boolean hasPrecedence(char op1, char op2) {
		return OPERATOR_LEVELS.get(op1) <= OPERATOR_LEVELS.get(op2);
	}

	// A utility method to apply an operator 'op' on values from numbers stack.
	// Result pushes back to stack
	protected static void applyOp(char op, Stack<Value> values, boolean useStrictLogic) {
		final Value b = values.pop();

		// Unary operations
		switch( op ){
			case '%':
				b.valueType = Value.Type.PERCENT;
				values.push(b);
				return;
			case '(':
				values.push(b);
				return;
		}

		final Value a = values.pop();

		final boolean leftPercent = ( a.valueType == Value.Type.PERCENT
				&& b.valueType != Value.Type.PERCENT );

		if( a.valueType != Value.Type.PERCENT
				&& b.valueType == Value.Type.PERCENT ){
			b.value = a.value * b.value / 100;
		}

		// Binary operations
		switch( op ){
			case '+':
				if( leftPercent ) throw new
						ArithmeticException("Percentages cannot be the left operand");
				a.value = a.value + b.value;
				break;
			case '-':
				if( leftPercent ) throw new
						ArithmeticException("Percentages cannot be the left operand");
				a.value = a.value - b.value;
				break;
			case '*':
				a.value = a.value * b.value;
				break;
			case '/':
				if( b.value == 0 ){
					if( useStrictLogic ) throw new ArithmeticException("Cannot divide by zero");
					else a.value = 0;
				} else {
					a.value = a.value / b.value;
				}
				break;
		}
		values.push(a);
	}

	public static class Value {
		enum Type {
			NUMBER, PERCENT
		}

		Type valueType;
		double value;

		public Value(Type valueType, double value) {
			this.valueType = valueType;
			this.value = value;
		}
	}
}
