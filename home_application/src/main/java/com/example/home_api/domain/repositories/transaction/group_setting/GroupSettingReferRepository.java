package com.example.home_api.domain.repositories.transaction.group_setting;

import com.example.home_api.domain.entities.transaction.group.GroupSettingEntity;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;

import java.util.List;

public interface GroupSettingReferRepository {

    List<GroupSettingEntity> find(GroupId groupId, GroupSettingKey key);

}
