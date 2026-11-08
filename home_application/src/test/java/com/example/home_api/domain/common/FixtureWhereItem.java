package com.example.home_api.domain.common;

import java.util.List;

class FixtureWhereItem {

    public static WhereItem NULL() {
        return null;
    }

    public static WhereItem 正常_値あり() {
        return WhereItem.from("column1", "value1");
    }

    public static WhereItem 正常_値空欄() {
        return WhereItem.from("column2", "");
    }

    public static WhereItem 正常_値NULL() {
        return WhereItem.from("column3", null);
    }

    public static WhereItem 正常_数値() {
        return WhereItem.from("column4", 10);
    }

    public static WhereItem 異常_カラム名空欄() {
        return WhereItem.from("", "value5");
    }

    public static WhereItem 異常_カラム名NULL() {
        return WhereItem.from(null, "value6");
    }

    public static List<WhereItem> リスト_NULL() {
        return null;
    }

    public static List<WhereItem> リスト_単一() {
        return List.of(FixtureWhereItem.正常_値あり());
    }

    public static List<WhereItem> リスト_複数() {
        return List.of(FixtureWhereItem.正常_値あり(), FixtureWhereItem.正常_値空欄(), FixtureWhereItem.正常_値NULL(),FixtureWhereItem.正常_数値());
    }

}