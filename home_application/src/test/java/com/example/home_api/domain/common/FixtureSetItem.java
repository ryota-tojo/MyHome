package com.example.home_api.domain.common;

import java.util.List;

class FixtureSetItem {

    public static SetItem NULL() {
        return null;
    }

    public static SetItem 正常_値あり_文字列() {
        return SetItem.from("column1", "value1");
    }

    public static SetItem 正常_値あり_数値() {
        return SetItem.from("column2", 10);
    }

    public static SetItem 正常_値あり_日付() {
        return SetItem.from("column3", "NOW()");
    }

    public static SetItem 正常_値空欄() {
        return SetItem.from("column4", "");
    }

    public static SetItem 正常_値NULL() {
        return SetItem.from("column5", null);
    }

    public static List<SetItem> リスト_NULL() {
        return null;
    }

    public static List<SetItem> リスト_単一() {
        return List.of(FixtureSetItem.正常_値あり_文字列());
    }

    public static List<SetItem> リスト_複数() {
        return List.of(FixtureSetItem.正常_値あり_文字列(), FixtureSetItem.正常_値あり_数値(), FixtureSetItem.正常_値あり_日付(), FixtureSetItem.正常_値空欄(), FixtureSetItem.正常_値NULL());
    }

}