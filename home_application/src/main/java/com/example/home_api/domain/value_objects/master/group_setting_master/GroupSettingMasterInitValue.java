package com.example.home_api.domain.value_objects.master.group_setting_master;

import com.example.home_api.common.ValidateUtils;
import lombok.Value;

@Value
public class GroupSettingMasterInitValue {
    String value;

    private GroupSettingMasterInitValue(String value) {
        this.value = value;
    }

    public static GroupSettingMasterInitValue of(String value){
        if (value == null) {
            return new GroupSettingMasterInitValue(null);
        }
        if (!ValidateUtils.isRequiredCheckForString(value)) {
            return new GroupSettingMasterInitValue(value);
        }
        return new GroupSettingMasterInitValue(value);
    }
}
