package com.example.home_api.domain.repositories.transaction.group;

import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.entities.transaction.group.GroupEntity;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group.GroupName;
import com.example.home_api.domain.value_objects.transaction.group.GroupPassword;

import java.util.List;

public interface GroupReferRepository {

    List<GroupEntity> find(GroupId groupId, GroupPassword groupPassword, GroupName name, Offset offset, Limit limit);

}
