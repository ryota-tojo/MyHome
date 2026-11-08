package com.example.home_api.domain.value_objects.master.user_setting_master;

import com.example.home_api.common.ValidateUtils;
import lombok.Value;

@Value
public class UserSettingMasterRemarks {
    String value;

    private UserSettingMasterRemarks(String value) {
        this.value = value;
    }

    public static UserSettingMasterRemarks of(String value){
        if (value == null) {
            return new UserSettingMasterRemarks(null);
        }
        if (!ValidateUtils.isRequiredCheckForString(value)) {
            return new UserSettingMasterRemarks(value);
        }
        return new UserSettingMasterRemarks(value);
    }
}
