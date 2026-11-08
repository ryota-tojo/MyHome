package com.example.home_api.domain.value_objects.master.group_setting_master;

import com.example.home_api.common.ValidateUtils;
import lombok.Value;

@Value
public class GroupSettingMasterRemarks {
    String value;

    private GroupSettingMasterRemarks(String value) {
        this.value = value;
    }

    public static GroupSettingMasterRemarks of(String value){
        if (value == null) {
            return new GroupSettingMasterRemarks(null);
        }
        if (!ValidateUtils.isRequiredCheckForString(value)) {
            return new GroupSettingMasterRemarks(value);
        }
        return new GroupSettingMasterRemarks(value);
    }
}
