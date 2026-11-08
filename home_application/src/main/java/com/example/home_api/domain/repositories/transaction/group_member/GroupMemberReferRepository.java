package com.example.home_api.domain.repositories.transaction.group_member;

import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.entities.transaction.group.GroupMemberEntity;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupApproveFlag;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupLeaderFlag;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupMemberId;
import com.example.home_api.domain.value_objects.transaction.user.UserId;

import java.util.List;

public interface GroupMemberReferRepository {

    List<GroupMemberEntity> find(GroupId groupId, UserId userId, GroupApproveFlag approveFlag, GroupLeaderFlag leaderFlag, Offset offset, Limit limit);

}
