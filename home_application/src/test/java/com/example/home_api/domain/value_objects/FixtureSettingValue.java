package com.example.home_api.domain.value_objects;

import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;

public class FixtureSettingValue {

    public static MasterSettingValue 通常() {
        return MasterSettingValue.of("setting_value");
    }

    public static MasterSettingValue 空欄() {
        return MasterSettingValue.of("");
    }

    public static MasterSettingValue NULL() {
        return MasterSettingValue.of(null);
    }

}