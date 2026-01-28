package com.example.urlshortener.util;

public class Base62Encoder {

    private static final String BASE62 =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String encode(int value) {
        value = Math.abs(value);
        StringBuilder result = new StringBuilder();

        while (value > 0) {
            result.append(BASE62.charAt(value % 62));
            value /= 62;
        }
        return result.toString();
    }
}
