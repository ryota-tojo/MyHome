package com.example.home_api.domain.repositories.transaction.group_setting;

import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingValue;

public interface GroupSettingCreateRepository {

    String create(GroupId groupId, GroupSettingKey key, GroupSettingValue value);

}
