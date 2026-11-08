package com.example.home_api.domain.repositories.master.group_setting_master;

import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;

public interface GroupSettingMasterCreateRepository {

    String create(GroupSettingMasterKey key, GroupSettingMasterInitValue value, GroupSettingMasterRemarks remarks);

}
