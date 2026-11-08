package com.example.home_api.domain.entities.transaction.user;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingValue;
import lombok.Value;

@Value
public class UserSettingEntity {
    UserId userId;
    UserSettingKey key;
    UserSettingValue value;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static UserSettingEntity of(
            UserId userId,
            UserSettingKey key,
            UserSettingValue value,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime) {

        return new UserSettingEntity(userId, key, value, createDatetime, updateDatetime);
    }
}
