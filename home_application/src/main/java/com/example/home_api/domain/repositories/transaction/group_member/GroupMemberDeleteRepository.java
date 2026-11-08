package com.example.home_api.domain.repositories.transaction.group_member;

import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.user.UserId;

public interface GroupMemberDeleteRepository {

    Integer delete(GroupId groupId, UserId userId);

}
