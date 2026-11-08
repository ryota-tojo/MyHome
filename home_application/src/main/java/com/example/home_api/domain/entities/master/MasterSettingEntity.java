package com.example.home_api.domain.entities.master;

import com.example.home_api.domain.value_objects.*;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;
import lombok.Value;

@Value
public class MasterSettingEntity {
    MasterSettingKey settingKey;
    MasterSettingValue settingValue;
    MasterSettingRemarks settingRemarks;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static MasterSettingEntity of(
            MasterSettingKey settingKey,
            MasterSettingValue settingValue,
            MasterSettingRemarks settingRemarks,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime) {
        return new MasterSettingEntity(settingKey, settingValue, settingRemarks, createDatetime, updateDatetime);
    }
}
