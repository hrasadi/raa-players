package media.raa.raa_android_player.model;

/**
 * Extends string logic for our specific purposes
 * Created by hamid on 10/2/17.
 */

public class StringHelper {
    private static char[] persianDigits = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};
    public static String convertToPersianLocaleString(String string) {
        for (int i = 0; i < persianDigits.length; i++) {
            string = string.replace(Character.forDigit(i, 10), persianDigits[i]);
        }
        return string;
    }
}
