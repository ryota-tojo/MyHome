package com.example.home_api.domain.value_objects.master.user_setting_master;

import com.example.home_api.common.ValidateUtils;
import lombok.Value;

@Value
public class UserSettingMasterInitValue {
    String value;

    private UserSettingMasterInitValue(String value) {
        this.value = value;
    }

    public static UserSettingMasterInitValue of(String value){
        if (value == null) {
            return new UserSettingMasterInitValue(null);
        }
        if (!ValidateUtils.isRequiredCheckForString(value)) {
            return new UserSettingMasterInitValue(value);
        }
        return new UserSettingMasterInitValue(value);
    }
}
