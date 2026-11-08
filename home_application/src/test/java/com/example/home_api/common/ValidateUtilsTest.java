package com.example.home_api.common;

import com.example.home_api.domain.value_objects.transaction.user.UserId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ValidateUtilsTest {

    @ParameterizedTest
    @MethodSource("isRequiredCheckForObjectCase")
    void isRequiredCheckForObject(String userCase, Object obj , boolean expected) {
        boolean result = ValidateUtils.isRequiredCheckForObject(obj);
        assertEquals(expected, result);
    }
    static Stream<Arguments> isRequiredCheckForObjectCase() {
        return Stream.of(
                Arguments.of("正常系: 中身が空のオブジェクト", new Object(), true),
                Arguments.of("正常系: 中身が空欄のオブジェクト", UserId.of(""), true),
                Arguments.of("正常系: 中身がnullのオブジェクト", UserId.of(null), true),
                Arguments.of("準正常系: null", null, false)
        );
    }

    @ParameterizedTest
    @MethodSource("isRequiredCheckForStringCase")
    void isRequiredCheckForString(String userCase, String str , boolean expected) {
        boolean result = ValidateUtils.isRequiredCheckForString(str);
        assertEquals(expected, result);
    }
    static Stream<Arguments> isRequiredCheckForStringCase() {
        return Stream.of(
                Arguments.of("正常系: 文字列あり", "abc", true),
                Arguments.of("準正常系: 空の文字列", "", false),
                Arguments.of("準正常系: 半角スペース", "   ", false),
                Arguments.of("準正常系: null", null, false)
        );
    }

}