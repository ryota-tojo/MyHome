package com.example.home_api.domain.value_objects.master.choice_master;

import com.example.home_api.common.ValidateUtils;
import lombok.Value;

@Value
public class ChoiceMasterSettingValue {
    String value;

    private ChoiceMasterSettingValue(String value) {
        this.value = value;
    }

    public static ChoiceMasterSettingValue of(String value){
        if (value == null) {
            return new ChoiceMasterSettingValue(null);
        }
        if (!ValidateUtils.isRequiredCheckForString(value)) {
            return new ChoiceMasterSettingValue(value);
        }
        return new ChoiceMasterSettingValue(value);
    }
}
