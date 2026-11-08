package com.example.home_api.domain.repositories.transaction.user_setting;

import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingValue;

public interface UserSettingCreateRepository {

    String create(UserId userId, UserSettingKey key, UserSettingValue value);

}
