package com.example.home_api.domain.value_objects.master.master_setting;

import com.example.home_api.common.ValidateUtils;
import lombok.Value;

@Value
public class MasterSettingRemarks {
    String value;

    private MasterSettingRemarks(String value) {
        this.value = value;
    }

    public static MasterSettingRemarks of(String value){
        if (value == null) {
            return new MasterSettingRemarks(null);
        }
        if (!ValidateUtils.isRequiredCheckForString(value)) {
            return new MasterSettingRemarks(value);
        }
        return new MasterSettingRemarks(value);
    }
}
