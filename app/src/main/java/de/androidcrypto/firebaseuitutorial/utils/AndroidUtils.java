package de.androidcrypto.firebaseuitutorial.utils;

public class AndroidUtils {

    public static String shortenString(String input, int maxLength) {
        if (input != null && input.length() > maxLength) {
            return input.substring(0, (maxLength - 3)) + " ..";
        } else {
            return input;
        }
    }
}
