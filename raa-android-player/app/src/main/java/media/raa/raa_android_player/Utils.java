package media.raa.raa_android_player;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * Extends string logic for our specific purposes
 * Created by hamid on 10/2/17.
 */

public class Utils {
    private static char[] persianDigits = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};

    public static String convertToPersianLocaleString(String string) {
        for (int i = 0; i < persianDigits.length; i++) {
            string = string.replace(Character.forDigit(i, 10), persianDigits[i]);
        }
        return string;
    }

    public static String getRelativeDayName(DateTime date) {
        if (date == null) {
            return null;
        }

        int days = Days.daysBetween(DateTime.now().withTimeAtStartOfDay(), date.withTimeAtStartOfDay()).getDays();
        switch (days) {
            case 0:
                return "امروز";
            case 1:
                return "فردا";
            case 2:
                return "پس‌فردا";
            case -1:
                return "دیروز";
            case -2:
                return "پریروز";
            default:
                return Utils.convertToPersianLocaleString(Integer.toString(date.getDayOfMonth())) + "ام";
        }
    }
}
