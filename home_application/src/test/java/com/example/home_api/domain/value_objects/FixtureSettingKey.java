package com.example.home_api.domain.value_objects;

import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;

public class FixtureSettingKey {

    public static MasterSettingKey 通常() {
        return MasterSettingKey.of("setting_key");
    }
    public static MasterSettingKey 空欄() {
        return MasterSettingKey.of("");
    }
    public static MasterSettingKey NULL() {
        return MasterSettingKey.of(null);
    }

}