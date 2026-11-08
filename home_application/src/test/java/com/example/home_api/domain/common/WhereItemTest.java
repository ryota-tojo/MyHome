package com.example.home_api.domain.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WhereItemTest {

    @Test
    void from() {
        assertEquals(FixtureWhereItem.正常_値あり(), WhereItem.from("column1", "value1"));
    }

    @Test
    void fromForNullError() {
        assertThrows(NullPointerException.class, () -> WhereItem.from(null, "value"));
    }

    @Test
    void fromForEmptyError() {
        assertThrows(NullPointerException.class, () -> WhereItem.from(null, "value"));
    }

    @ParameterizedTest
    @MethodSource({"testCaseForAndOnly","testCaseForOrOnly","testCaseForMix"})
    void create(String userCase, List<WhereItem> andItems, List<WhereItem> orItems, String expected) {
        String result = WhereItem.create(andItems, orItems);
        System.out.println("期待: " + expected);
        System.out.println("実績: " + result);
        assertEquals(expected, result);
    }

    static Stream<Arguments> testCaseForAndOnly() {
        return Stream.of(
                Arguments.of("【正常系】NULLのリスト", FixtureWhereItem.リスト_NULL(), FixtureWhereItem.リスト_NULL(), ""),
                Arguments.of("【正常系】単一のリスト_正常値", FixtureWhereItem.リスト_単一(), FixtureWhereItem.リスト_NULL(), " WHERE column1='value1'"),
                Arguments.of("【正常系】複数のリスト", FixtureWhereItem.リスト_複数(), FixtureWhereItem.リスト_NULL(), " WHERE column1='value1' AND column2='' AND column3 IS NULL AND column4=10")
        );
    }

    static Stream<Arguments> testCaseForOrOnly() {
        return Stream.of(
                Arguments.of("【正常系】NULLのリスト", FixtureWhereItem.リスト_NULL(),FixtureWhereItem.リスト_NULL(),  ""),
                Arguments.of("【正常系】単一のリスト_正常値", FixtureWhereItem.リスト_NULL(),FixtureWhereItem.リスト_単一(),  " WHERE column1='value1'"),
                Arguments.of("【正常系】複数のリスト",  FixtureWhereItem.リスト_NULL(),FixtureWhereItem.リスト_複数(), " WHERE column1='value1' OR column2='' OR column3 IS NULL OR column4=10")
        );
    }

    static Stream<Arguments> testCaseForMix() {
        return Stream.of(
                Arguments.of("【正常系】AND-NULLのリスト_OR-単一のリスト", FixtureWhereItem.リスト_NULL(),FixtureWhereItem.リスト_単一(),  " WHERE column1='value1'"),
                Arguments.of("【正常系】AND-NULLのリスト_OR-複数のリスト", FixtureWhereItem.リスト_NULL(),FixtureWhereItem.リスト_複数(),  " WHERE column1='value1' OR column2='' OR column3 IS NULL OR column4=10"),
                Arguments.of("【正常系】AND-単一のリスト_OR-単一のリスト", FixtureWhereItem.リスト_単一(),FixtureWhereItem.リスト_単一(),  " WHERE column1='value1' AND column1='value1'"),
                Arguments.of("【正常系】AND-単一のリスト_OR-複数のリスト", FixtureWhereItem.リスト_単一(),FixtureWhereItem.リスト_複数(),  " WHERE column1='value1' AND (column1='value1' OR column2='' OR column3 IS NULL OR column4=10)"),
                Arguments.of("【正常系】AND-複数のリスト_OR-単一のリスト", FixtureWhereItem.リスト_複数(),FixtureWhereItem.リスト_単一(),  " WHERE column1='value1' AND column2='' AND column3 IS NULL AND column4=10 AND column1='value1'"),
                Arguments.of("【正常系】AND-複数のリスト_OR-複数のリスト", FixtureWhereItem.リスト_複数(),FixtureWhereItem.リスト_複数(),  " WHERE column1='value1' AND column2='' AND column3 IS NULL AND column4=10 AND (column1='value1' OR column2='' OR column3 IS NULL OR column4=10)")
        );
    }
}