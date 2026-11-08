package com.example.home_api.domain.repositories.master.group_setting_master;

import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;

public interface GroupSettingMasterDeleteRepository {

    Integer delete(GroupSettingMasterKey key);

}
