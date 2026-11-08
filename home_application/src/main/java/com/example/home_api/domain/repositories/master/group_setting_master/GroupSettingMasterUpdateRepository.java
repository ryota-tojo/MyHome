package com.example.home_api.domain.repositories.master.group_setting_master;

import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterRemarks;

public interface GroupSettingMasterUpdateRepository {

    Integer update(GroupSettingMasterKey key, GroupSettingMasterInitValue value, GroupSettingMasterRemarks remarks);

}
