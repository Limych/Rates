package ru.khrolenok.exchangerates;

/**
 * Created by Limych on 07.07.2015.
 */
public class Settings {
    public static final String APP_ID = "ru.khrolenok.exchangerates";

    public static final String PREFS_NAME = APP_ID;
    public static final String LOG_TAG = "Exchange Rates";

    interface Display {
        static final double changesThreshold = 0.1;    // Percents

        static final String colorUp   = "colorUp";
        static final String colorDown = "colorDown";

        static final String rateFormat = "rateFormat";
        static final String ratesList = "ratesList";

        static final String ratesListDefault =
                "USDRUB_CBR, EURRUB_CBR, USDRUB_TOM, EURRUB_TOM, USDRUB_FRX, EURRUB_FRX";
    }

    interface Rates {
        static final String ratesKey = "Rates";

        static final int reloadDelay = 3 * 60 * 60 * 1000;  // every 3 hours

        static final String sourceUrl = "http://khrolenok.ru/tools/exchange_rates.php";

        interface Types {
            public static final String OFFICIAL = "CBR";
            public static final String STOCK    = "STK";
            public static final String FOREX    = "FRX";
        }

        interface Columns {
            public static final int CROSS_TYPE  = 0;
            public static final int TIMESTAMP   = 1;
            public static final int FACE_VALUE  = 2;
            public static final int INITIAL_BID = 3;
            public static final int LAST_BID    = 4;
        }
    }
}
