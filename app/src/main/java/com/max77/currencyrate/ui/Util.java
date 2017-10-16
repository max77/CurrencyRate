package com.max77.currencyrate.ui;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Date;

/**
 * Created by mkomarovskiy on 13/10/2017.
 */

class Util {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    static boolean isSameBankingDay(Date d1, Date d2) {
        if (d1 == null || d2 == null)
            return false;

        long day1 = d1.getTime() / MILLIS_PER_DAY;
        long day2 = d2.getTime() / MILLIS_PER_DAY;

        return day1 == day2;

        // TODO: holidays/weekends not handled!!!
    }

    static String getPrettyDate(Context context, Date d) {
        return DateFormat.getDateFormat(context).format(d);
    }
}
