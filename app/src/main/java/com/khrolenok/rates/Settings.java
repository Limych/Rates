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

/**
 * Created by Limych on 07.07.2015
 */
public class Settings {
	public static final String IDEA_SUGGEST_URL = "http://exrates.reformal.ru/";

	public interface Display {
		double CHANGES_THRESHOLD = 0.1;    // Percents
	}

	public interface Rates {

		int reloadDelay = 3 * 60 * 60 * 1000;  // every 3 hours

		interface Groups {
			String OFFICIAL = "CBR";
			String STOCK = "STK";
			String FOREX = "FRX";
		}

		interface Columns {
			int GROUP = 0;
			int GOOD = 1;
			int CURRENCY = 2;
			int TIMESTAMP = 3;
			int FACE_VALUE = 4;
			int INITIAL_BID = 5;
			int LAST_BID = 6;
			int HIGH_BID = 7;
			int LOW_BID = 8;
		}
	}
}
