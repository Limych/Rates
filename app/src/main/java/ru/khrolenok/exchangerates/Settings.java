package ru.khrolenok.exchangerates;

/**
 * Created by Limych on 07.07.2015.
 */
public class Settings {
    public static final String APP_ID = "ru.khrolenok.exchangerates";
    public static final String TAG = "ExRates";

    public static final String PREFS_NAME = APP_ID;

    public interface Display {
        static final double changesThreshold = 0.1;    // Percents

        static final String colorUp   = "colorUp";
        static final String colorDown = "colorDown";

        static final String rateFormat = "rateFormat";
        static final String ratesList = "ratesList";

        static final String ratesListDefault =
                "CBR_USD_RUB, CBR_EUR_RUB, STK_USD_RUB, STK_EUR_RUB, FRX_USD_RUB, FRX_EUR_RUB";
    }

    public interface Rates {
        static final String ratesKey = "Rates";

        static final int reloadDelay = 3 * 60 * 60 * 1000;  // every 3 hours

        static final String sourceUrl = "http://khrolenok.ru/tools/exchange_rates.php";

        interface Groups {
            public static final String OFFICIAL = "CBR";
            public static final String STOCK    = "STK";
            public static final String FOREX    = "FRX";
        }

        interface Columns {
            public static final int GROUP       = 0;
            public static final int GOOD        = 1;
            public static final int CURRENCY    = 2;
            public static final int TIMESTAMP   = 3;
            public static final int FACE_VALUE  = 4;
            public static final int INITIAL_BID = 5;
            public static final int LAST_BID    = 6;
            public static final int HIGH_BID    = 7;
            public static final int LOW_BID     = 8;
        }
    }

    public interface Preferences {
        static String invertColors  = "invert_colors";
    }
}
