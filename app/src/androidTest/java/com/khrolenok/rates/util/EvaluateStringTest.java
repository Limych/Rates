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

package com.khrolenok.rates.util;

import junit.framework.TestCase;

/**
 * Created by Limych on 06.09.2015
 */
public class EvaluateStringTest extends TestCase {

	public void testEvaluate() throws Exception {
		assertEquals(EvaluateString.evaluate("12.3 + 2 * 6"), ( 12.3 + 2 * 6 ));
		assertEquals(EvaluateString.evaluate("12.3 * 2 + 12"), ( 12.3 * 2 + 12 ));
		assertEquals(EvaluateString.evaluate("12.3 * ( 2 + 12 )"), ( 12.3 * ( 2 + 12 ) ));
		assertEquals(EvaluateString.evaluate("12.3 * ( 2 + 12 ) / 14"), ( 12.3 * ( 2 + 12 ) / 14 ));
		assertEquals(EvaluateString.evaluate("12.3 + 10%"), ( 12.3 + 12.3 * 0.1 ));
		assertEquals(EvaluateString.evaluate("12.3 + 10% * 2"), ( 12.3 + 12.3 * 0.2 ));
		assertEquals(EvaluateString.evaluate("12.3 + (10 + 10)%"), ( 12.3 + 12.3 * 0.2 ));
		assertEquals(EvaluateString.evaluate("12.3 + (10 - 10)%"), ( 12.3 + 12.3 * 0.0 ));
		assertEquals(EvaluateString.evaluate("12.3 × 10 * 10%"), ( ( 12.3 * 10 ) * ( ( 12.3 * 10 ) * 0.1 ) ));
	}

	public void testEvaluateNonStrict() throws Exception {
		assertEquals(EvaluateString.evaluate("12.3 + * 2", false), ( 12.3 * 2 ));
		assertEquals(EvaluateString.evaluate("12.3 + (2 * (2", false), ( 12.3 + ( 2 * 2 ) ));
		assertEquals(EvaluateString.evaluate("12.3 + (2 * (", false), ( 12.3 + 2 ));
		assertEquals(EvaluateString.evaluate("12.3 * (1 + 1) )", false), ( 12.3 * ( 1 + 1 ) ));
	}

	private void assertArithmeticException(String expression) {
		try{
			EvaluateString.evaluate(expression);
			fail("Should have thrown UnsupportedOperation exception");
		} catch( ArithmeticException ignored ){
		}
	}

	public void testEvaluateFails() {
		assertArithmeticException("12.3 / 0");
		assertArithmeticException("12.3 / (2 - 2)");
		assertArithmeticException("12.3 + (10% - 2)");
		assertArithmeticException("12.3 + (10% + 2)");
	}
}