package com.randomname.vlad.nasheradio.util;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String removeURLFromString(String input) {

        String urlRegex = "((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?";

        String output = input.replaceAll(urlRegex, "");

        return output;
    }

    public static String replaceVkLink(String input) {
        String output = input;

        String regex = "\\Q[\\E(.+)\\Q|\\E(.+)\\Q]\\E";
        Pattern p = Pattern.compile(regex);

        String id;
        String name;

        Matcher m = p.matcher(input);
        if (m.find()) {
            MatchResult mr = m.toMatchResult();
            id = mr.group(1);
            name = mr.group(2);

            String replacement = "<a href=\"https://vk.com/" + id + "\">" + name + "</a>";
            output = input.replaceAll(regex, replacement);
        }

        return output;
    }
}
