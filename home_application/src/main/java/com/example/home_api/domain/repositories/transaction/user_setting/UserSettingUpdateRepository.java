package com.example.home_api.domain.repositories.transaction.user_setting;

import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingValue;

public interface UserSettingUpdateRepository {

    Integer update(UserId userId, UserSettingKey key, UserSettingValue value);

}
