package com.example.home_api.domain.common;

import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlAndParamsTest {

    @ParameterizedTest
    @MethodSource("fromCase")
    void from(String userCase, MasterSettingKey key, Integer offset, Integer limit, SqlAndParams expected) {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    key,
                    value,
                    remarks,
                    create_date,
                    update_date
                FROM master_setting
                """);

        List<Object> params = new ArrayList<>();

        if (key != null && key.getValue() != null && !key.getValue().isEmpty()) {
            sql.append(" WHERE key = ?");
            params.add(key.getValue());
        }

        if (offset != null) {
            sql.append(" OFFSET ?");
            params.add(offset);
        }

        if (limit != null) {
            sql.append(" LIMIT ?");
            params.add(limit);
        }

        assertEquals(expected, SqlAndParams.from(sql.toString(), params));
    }

    static Stream<Arguments> fromCase() {
        return Stream.of(
                Arguments.of(
                        "正常系",
                        MasterSettingKey.of("SettingKey"),
                        1,
                        2,
                        new SqlAndParams(
                                """
                                        SELECT
                                            key,
                                            value,
                                            remarks,
                                            create_date,
                                            update_date
                                        FROM master_setting
                                         WHERE key = ? OFFSET ? LIMIT ?""",
                                List.of("SettingKey", 1, 2)
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testCaseForSelect")
    void toSqlAndParamsForSelect(String userCase, List<WhereItem> andItems, List<WhereItem> orItems, Offset offset, Limit limit, SqlAndParams expected) {
        SqlAndParams result = SqlAndParams.toSqlAndParamsForSelect("tableName", List.of("c1,c2,c3"), andItems, orItems, offset, limit);
        System.out.println(result.getSql());
        System.out.println(result.getParams());
        assertEquals(expected, result);
    }

    static Stream<Arguments> testCaseForSelect() {
        return Stream.of(
                Arguments.of("【正常系】条件なし", null, null, null, null,
                        SqlAndParams.from("SELECT c1,c2,c3 FROM tableName;", List.of())),

                Arguments.of("【正常系】WHERE句_ANDのみ", FixtureWhereItem.リスト_単一(), null, null, null,
                        SqlAndParams.from("SELECT c1,c2,c3 FROM tableName WHERE column1= ?;", List.of("value1"))),

                Arguments.of("【正常系】WHERE句_ORのみ", null, FixtureWhereItem.リスト_複数(), null, null,
                        SqlAndParams.from("SELECT c1,c2,c3 FROM tableName WHERE column1= ? OR column2= ? OR column3 IS NULL OR column4= ?;", List.of("value1", "", 10))),

                Arguments.of("【正常系】WHERE句_混在", FixtureWhereItem.リスト_単一(), FixtureWhereItem.リスト_複数(), null, null,
                        SqlAndParams.from("SELECT c1,c2,c3 FROM tableName WHERE column1= ? AND (column1= ? OR column2= ? OR column3 IS NULL OR column4= ?);", List.of("value1", "value1", "", 10))),

                Arguments.of("【正常系】LIMIT", FixtureWhereItem.リスト_単一(), FixtureWhereItem.リスト_複数(), null, Limit.of(2),
                        SqlAndParams.from("SELECT c1,c2,c3 FROM tableName WHERE column1= ? AND (column1= ? OR column2= ? OR column3 IS NULL OR column4= ?) LIMIT ?;", List.of("value1", "value1", "", 10, 2))),

                Arguments.of("【正常系】OFFSET", FixtureWhereItem.リスト_単一(), FixtureWhereItem.リスト_複数(), Offset.of(1), null,
                        SqlAndParams.from("SELECT c1,c2,c3 FROM tableName WHERE column1= ? AND (column1= ? OR column2= ? OR column3 IS NULL OR column4= ?) OFFSET ?;", List.of("value1", "value1", "", 10, 1))),

                Arguments.of("【正常系】ALL", FixtureWhereItem.リスト_単一(), FixtureWhereItem.リスト_複数(), Offset.of(1), Limit.of(2),
                        SqlAndParams.from("SELECT c1,c2,c3 FROM tableName WHERE column1= ? AND (column1= ? OR column2= ? OR column3 IS NULL OR column4= ?) OFFSET ? LIMIT ?;", List.of("value1", "value1", "", 10, 1, 2)))
        );
    }

    @ParameterizedTest
    @MethodSource("testCaseForInsert")
    void toSqlAndParamsForInsert(String userCase, List<String> columns, List<Object> values, SqlAndParams expected) {
        SqlAndParams result = SqlAndParams.toSqlAndParamsForInsert("tableName", columns, values);
        System.out.println("期待: " + expected.getSql());
        System.out.println("実際: " + result.getSql());
        System.out.println("期待: " + expected.getParams());
        System.out.println("実際: " + result.getParams());
        assertEquals(expected, result);
    }

    static Stream<Arguments> testCaseForInsert() {
        return Stream.of(
                Arguments.of("【正常系】単一_文字列", List.of("c1"), List.of("value1"),
                        SqlAndParams.from("INSERT INTO tableName(c1) VALUES(?);", List.of("value1"))),

                Arguments.of("【正常系】単一_数値", List.of("c1"), List.of(10),
                        SqlAndParams.from("INSERT INTO tableName(c1) VALUES(?);", List.of(10))),

                Arguments.of("【正常系】単一_NULL", List.of("c1"), List.of("NULL"),
                        SqlAndParams.from("INSERT INTO tableName(c1) VALUES(NULL);", List.of())),

                Arguments.of("【正常系】単一_CURRENT_TIMESTAMP", List.of("c1"), List.of("CURRENT_TIMESTAMP"),
                        SqlAndParams.from("INSERT INTO tableName(c1) VALUES(CURRENT_TIMESTAMP);", List.of())),

                Arguments.of("【正常系】複数", List.of("c1,c2,c3,c4"), List.of("value1", 10, "NULL", "CURRENT_TIMESTAMP"),
                        SqlAndParams.from("INSERT INTO tableName(c1,c2,c3,c4) VALUES(?, ?, NULL, CURRENT_TIMESTAMP);", List.of("value1", 10)))
        );
    }

    @ParameterizedTest
    @MethodSource("testCaseForUpdate")
    void toSqlAndParamsForUpdate(String userCase, List<SetItem> setItems, List<WhereItem> andItems, List<WhereItem> orItems, SqlAndParams expected) {
        SqlAndParams result = SqlAndParams.toSqlAndParamsForUpdate("tableName", setItems, andItems, orItems);
        System.out.println("期待: " + expected.getSql());
        System.out.println("実際: " + result.getSql());
        System.out.println("期待: " + expected.getParams());
        System.out.println("実際: " + result.getParams());
        assertEquals(expected, result);
    }

    static Stream<Arguments> testCaseForUpdate() {
        return Stream.of(
                Arguments.of("【正常系】単一_文字列", FixtureSetItem.リスト_単一(), null, null,
                        SqlAndParams.from("UPDATE tableName SET column1=?;", List.of("value1"))),

                Arguments.of("【正常系】複数", FixtureSetItem.リスト_複数(), null, null,
                        SqlAndParams.from("UPDATE tableName SET column1=?, column2=?, column3=CURRENT_TIMESTAMP, column4=?, column5=NULL;", List.of("value1", 10, ""))),

                Arguments.of("【正常系】複数_単一AND条件", FixtureSetItem.リスト_複数(), FixtureWhereItem.リスト_単一(), null,
                        SqlAndParams.from("UPDATE tableName SET column1=?, column2=?, column3=CURRENT_TIMESTAMP, column4=?, column5=NULL WHERE column1=?;", List.of("value1", 10, "", "value1"))),

                Arguments.of("【正常系】複数_単一OR条件", FixtureSetItem.リスト_複数(), null, FixtureWhereItem.リスト_単一(),
                        SqlAndParams.from("UPDATE tableName SET column1=?, column2=?, column3=CURRENT_TIMESTAMP, column4=?, column5=NULL WHERE column1=?;", List.of("value1", 10, "", "value1"))),

                Arguments.of("【正常系】複数_単一ANDOR条件", FixtureSetItem.リスト_複数(), FixtureWhereItem.リスト_単一(), FixtureWhereItem.リスト_複数(),
                        SqlAndParams.from("UPDATE tableName SET column1=?, column2=?, column3=CURRENT_TIMESTAMP, column4=?, column5=NULL WHERE column1=? AND (column1=? OR column2=? OR column3 IS NULL OR column4=?);", List.of("value1", 10, "", "value1", "value1", "", 10)))
        );
    }

    @ParameterizedTest
    @MethodSource("testCaseForDelete")
    void toSqlAndParamsForDelete(String userCase, List<WhereItem> andItems, List<WhereItem> orItems, SqlAndParams expected) {
        SqlAndParams result = SqlAndParams.toSqlAndParamsForDelete("tableName", andItems, orItems);
        System.out.println("期待: " + expected.getSql());
        System.out.println("実際: " + result.getSql());
        System.out.println("期待: " + expected.getParams());
        System.out.println("実際: " + result.getParams());
        assertEquals(expected, result);
    }

    static Stream<Arguments> testCaseForDelete() {
        return Stream.of(
                Arguments.of("【正常系】条件なし", null, null,
                        SqlAndParams.from("DELETE FROM tableName;", List.of())),

                Arguments.of("【正常系】単一AND条件", FixtureWhereItem.リスト_単一(), null,
                        SqlAndParams.from("DELETE FROM tableName WHERE column1=?;", List.of("value1"))),

                Arguments.of("【正常系】単一OR条件", null, FixtureWhereItem.リスト_単一(),
                        SqlAndParams.from("DELETE FROM tableName WHERE column1=?;", List.of("value1"))),

                Arguments.of("【正常系】単一_数値", FixtureWhereItem.リスト_単一(), FixtureWhereItem.リスト_複数(),
                        SqlAndParams.from("DELETE FROM tableName WHERE column1=? AND (column1=? OR column2=? OR column3 IS NULL OR column4=?);", List.of("value1", "value1", "", 10)))
        );
    }
}