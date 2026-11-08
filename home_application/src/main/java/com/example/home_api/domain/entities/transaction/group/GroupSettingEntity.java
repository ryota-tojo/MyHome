package com.example.home_api.domain.entities.transaction.group;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingValue;
import lombok.Value;

@Value
public class GroupSettingEntity {
    GroupSettingId id;
    GroupId groupId;
    GroupSettingKey key;
    GroupSettingValue value;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static GroupSettingEntity of(
            GroupSettingId id,
            GroupId groupId,
            GroupSettingKey key,
            GroupSettingValue value,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime) {

        return new GroupSettingEntity(id, groupId, key, value, createDatetime, updateDatetime);
    }
}
