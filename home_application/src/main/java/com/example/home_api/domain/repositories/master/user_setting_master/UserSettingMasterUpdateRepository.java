package com.example.home_api.domain.repositories.master.user_setting_master;

import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterRemarks;

public interface UserSettingMasterUpdateRepository {

    Integer update(UserSettingMasterKey key, UserSettingMasterInitValue value, UserSettingMasterRemarks remarks);

}
