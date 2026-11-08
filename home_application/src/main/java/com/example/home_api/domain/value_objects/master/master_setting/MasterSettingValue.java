package com.example.home_api.domain.value_objects.master.master_setting;

import com.example.home_api.common.ValidateUtils;
import lombok.Value;

@Value
public class MasterSettingValue {
    String value;

    private MasterSettingValue(String value) {
        this.value = value;
    }

    public static MasterSettingValue of(String value){
        if (value == null) {
            return new MasterSettingValue(null);
        }
        if (!ValidateUtils.isRequiredCheckForString(value)) {
            return new MasterSettingValue(value);
        }
        return new MasterSettingValue(value);
    }
}
