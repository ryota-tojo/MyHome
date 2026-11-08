package com.example.home_api.domain.value_objects;

import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;

public class FixtureSettingRemarks {

    public static MasterSettingRemarks 通常() {
        return MasterSettingRemarks.of("setting_remarks");
    }

    public static MasterSettingRemarks 空欄() {
        return MasterSettingRemarks.of("");
    }

    public static MasterSettingRemarks NULL() {
        return MasterSettingRemarks.of(null);
    }
}