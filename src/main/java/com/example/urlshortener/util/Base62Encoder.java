package com.example.urlshortener.util;

public class Base62Encoder {

    private static final String BASE62 =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String encode(int value) {
        long v = value;
        return encode(v);
    }

    public static String encode(long value) {
        // treat value as unsigned
        long v = value;
        if (v == 0) {
            return String.valueOf(BASE62.charAt(0));
        }

        StringBuilder result = new StringBuilder();
        while (v > 0) {
            int idx = (int) (v % 62);
            result.append(BASE62.charAt(idx));
            v /= 62;
        }
        return result.toString();
    }
}
