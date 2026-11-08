package com.example.home_api.domain.entities.master;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterRemarks;
import lombok.Value;

@Value
public class UserSettingMasterEntity {
    UserSettingMasterKey key;
    UserSettingMasterInitValue initValue;
    UserSettingMasterRemarks remarks;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static UserSettingMasterEntity of(
            UserSettingMasterKey settingKey,
            UserSettingMasterInitValue settingValue,
            UserSettingMasterRemarks settingRemarks,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime) {

        return new UserSettingMasterEntity(settingKey, settingValue, settingRemarks, createDatetime, updateDatetime);
    }
}
