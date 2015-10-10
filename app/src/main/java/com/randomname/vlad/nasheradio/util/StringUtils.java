package com.randomname.vlad.nasheradio.util;

public class StringUtils {

    public static String removeURLFromString(String input) {

        String regex = "((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

        String output = input.replaceAll(regex, "");

        return output;
    }
}
