package com.example.home_api.domain.entities.transaction.group;

import com.example.home_api.domain.value_objects.ApproveDatetime;
import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupApproveFlag;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupLeaderFlag;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupMemberId;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import lombok.Value;

@Value
public class GroupMemberEntity {
    GroupMemberId id;
    GroupId groupId;
    UserId userId;
    GroupApproveFlag approveFlag;
    GroupLeaderFlag leaderFlag;
    ApproveDatetime approveDatetime;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static GroupMemberEntity of(
            GroupMemberId id,
            GroupId groupId,
            UserId userId,
            GroupApproveFlag approveFlag,
            GroupLeaderFlag leaderFlag,
            ApproveDatetime approveDatetime,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new GroupMemberEntity(
                id,
                groupId,
                userId,
                approveFlag,
                leaderFlag,
                approveDatetime,
                createDatetime,
                updateDatetime
        );
    }
}
