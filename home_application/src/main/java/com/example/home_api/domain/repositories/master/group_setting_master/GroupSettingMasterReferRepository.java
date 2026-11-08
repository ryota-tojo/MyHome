package com.example.home_api.domain.repositories.master.group_setting_master;

import com.example.home_api.domain.entities.master.GroupSettingMasterEntity;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;

import java.util.List;

public interface GroupSettingMasterReferRepository {

    List<GroupSettingMasterEntity> find(GroupSettingMasterKey key);

}
