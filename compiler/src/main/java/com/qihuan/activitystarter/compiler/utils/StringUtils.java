package com.qihuan.activitystarter.compiler.utils;

/**
 * StringUtils
 *
 * @author qi
 * @since 2021/8/4
 */
public class StringUtils {
    public static String camelToUnderline(String str) {
        int length = str.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_");
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty() || Character.isLowerCase(str.charAt(0))) {
            return null;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
