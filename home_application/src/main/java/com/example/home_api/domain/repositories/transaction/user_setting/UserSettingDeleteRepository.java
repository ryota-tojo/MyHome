package com.example.home_api.domain.repositories.transaction.user_setting;

import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;

public interface UserSettingDeleteRepository {

    Integer delete(UserId userId, UserSettingKey key);

}
