package com.example.home_api.domain.entities.master;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterRemarks;
import lombok.Value;

@Value
public class GroupSettingMasterEntity {
    GroupSettingMasterKey key;
    GroupSettingMasterInitValue initValue;
    GroupSettingMasterRemarks remarks;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static GroupSettingMasterEntity of(
            GroupSettingMasterKey key,
            GroupSettingMasterInitValue initValue,
            GroupSettingMasterRemarks remarks,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime) {

        return new GroupSettingMasterEntity(key, initValue, remarks, createDatetime, updateDatetime);
    }
}
