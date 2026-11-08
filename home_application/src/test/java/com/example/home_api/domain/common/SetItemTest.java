package com.example.home_api.domain.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SetItemTest {

    @Test
    void from() {
        assertEquals(FixtureSetItem.正常_値あり_文字列(), SetItem.from("column1", "value1"));
    }

    @Test
    void fromForNullError() {
        assertThrows(NullPointerException.class, () -> SetItem.from(null, "value"));
    }

    @Test
    void fromForEmptyError() {
        assertThrows(NullPointerException.class, () -> SetItem.from(null, "value"));
    }

    @Test
    void createForNullError() {
        assertThrows(NullPointerException.class, () -> SetItem.create(null));
    }

    @Test
    void createForEmptyError() {
        assertThrows(NullPointerException.class, () -> SetItem.create(FixtureSetItem.リスト_NULL()));
    }

    @ParameterizedTest
    @MethodSource({"testCase"})
    void create(String userCase, List<SetItem> setItems, String expected) {
        String result = SetItem.create(setItems);
        System.out.println("期待: " + expected);
        System.out.println("実績: " + result);
        assertEquals(expected, result);
    }

    static Stream<Arguments> testCase() {
        return Stream.of(
                Arguments.of("【正常系】単一のリスト", FixtureSetItem.リスト_単一(), " SET column1='value1'"),
                Arguments.of("【正常系】複数のリスト", FixtureSetItem.リスト_複数(), " SET column1='value1', column2=10, column3=NOW(), column4='', column5=NULL")
        );
    }

}