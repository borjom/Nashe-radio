package com.randomname.vlad.nasheradio.util;

public class StringUtils {

    public static String removeURLFromString(String input) {

        String urlRegex = "((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

        String output = input.replaceAll(urlRegex, "");

        return output;
    }
}
