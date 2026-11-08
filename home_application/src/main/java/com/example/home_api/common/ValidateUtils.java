package com.example.home_api.common;

public class ValidateUtils {

    // 必須チェック: オブジェクト
    public static boolean isRequiredCheckForObject(Object obj){
        return obj != null;
    }

    // 必須チェック: 文字列
    public static boolean isRequiredCheckForString(String value) {
        return !isBlank(value);
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isContainsInvalidChars(String value) {
        if (value == null) return false;

        return value.contains("<")
                || value.contains(">")
                || value.contains("'")
                || value.contains("\"");
    }

}
