package com.example.home_api.domain.repositories.transaction.user_setting;

import com.example.home_api.domain.entities.transaction.user.UserSettingEntity;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;

import java.util.List;

public interface UserSettingReferRepository {

    List<UserSettingEntity> find(UserId userId, UserSettingKey key);

}
