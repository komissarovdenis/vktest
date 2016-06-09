package com.test.ui.util;

public final class StringUtils {
    private StringUtils() {}

    public static boolean isBlank(String string) {
        return string == null || "".equals(string);
    }
}
