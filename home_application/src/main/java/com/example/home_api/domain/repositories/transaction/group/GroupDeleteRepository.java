package com.example.home_api.domain.repositories.transaction.group;

import com.example.home_api.domain.value_objects.transaction.group.GroupId;

public interface GroupDeleteRepository {

    Integer delete(GroupId groupId);

}
