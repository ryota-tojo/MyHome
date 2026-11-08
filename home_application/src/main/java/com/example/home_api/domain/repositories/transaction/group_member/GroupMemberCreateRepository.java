package com.example.home_api.domain.repositories.transaction.group_member;

import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group.GroupName;
import com.example.home_api.domain.value_objects.transaction.group.GroupPassword;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupApproveFlag;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupLeaderFlag;
import com.example.home_api.domain.value_objects.transaction.user.UserId;

public interface GroupMemberCreateRepository {

    String create(GroupId groupId, UserId userId, GroupApproveFlag approveFlag, GroupLeaderFlag leaderFlag);

}
