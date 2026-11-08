package com.example.home_api.domain.repositories.transaction.group;

import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group.GroupName;
import com.example.home_api.domain.value_objects.transaction.group.GroupPassword;

public interface GroupUpdateRepository {

    Integer update(GroupId groupId, GroupPassword password, GroupName groupName);

}
