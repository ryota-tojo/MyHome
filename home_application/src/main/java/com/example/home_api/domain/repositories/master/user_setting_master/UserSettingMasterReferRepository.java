package com.example.home_api.domain.repositories.master.user_setting_master;

import com.example.home_api.domain.entities.master.UserSettingMasterEntity;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;

import java.util.List;

public interface UserSettingMasterReferRepository {

    List<UserSettingMasterEntity> find(UserSettingMasterKey key);

}
