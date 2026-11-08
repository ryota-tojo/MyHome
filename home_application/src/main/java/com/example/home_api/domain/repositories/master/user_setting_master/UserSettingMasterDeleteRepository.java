package com.example.home_api.domain.repositories.master.user_setting_master;

import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;

public interface UserSettingMasterDeleteRepository {

    Integer delete(UserSettingMasterKey key);

}
