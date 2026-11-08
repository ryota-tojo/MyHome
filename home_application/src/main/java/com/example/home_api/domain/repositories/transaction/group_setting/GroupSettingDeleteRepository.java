package com.example.home_api.domain.repositories.transaction.group_setting;

import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;

public interface GroupSettingDeleteRepository {

    Integer delete(GroupId groupId, GroupSettingKey key);

}
