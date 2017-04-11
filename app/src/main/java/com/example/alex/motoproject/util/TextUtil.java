package com.example.alex.motoproject.util;

public class TextUtil {
    public static String trim(String toTrim) {
        return toTrim.trim().replaceAll(" +", " ").replaceAll("\\n+", "\n");
    }

    public static boolean hasText(String string) {
        return string.length() > 0 && !string.matches("\\s+");
    }
}
