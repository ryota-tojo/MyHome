package com.example.home_api.domain.entities.transaction.group;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group.GroupName;
import com.example.home_api.domain.value_objects.transaction.group.GroupPassword;
import lombok.Value;

@Value
public class GroupEntity {
    GroupId groupId;
    GroupPassword password;
    GroupName groupName;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static GroupEntity of(
            GroupId groupId,
            GroupPassword password,
            GroupName groupName,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new GroupEntity(
                groupId,
                password,
                groupName,
                createDatetime,
                updateDatetime
        );
    }
}
