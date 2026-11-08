package com.example.home_api.service.transaction.group_member;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.entities.transaction.group.GroupEntity;
import com.example.home_api.domain.entities.transaction.group.GroupMemberEntity;
import com.example.home_api.domain.entities.transaction.user.UserEntity;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.repositories.transaction.group_member.GroupMemberReferRepository;
import com.example.home_api.domain.repositories.transaction.user.UserReferRepository;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupApproveFlag;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupLeaderFlag;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMemberReferService {

    GroupReferRepository groupReferRepository;
    GroupMemberReferRepository groupMemberReferRepository;
    UserReferRepository userReferRepository;

    public GroupMemberReferService(
            GroupReferRepository groupReferRepository,
            GroupMemberReferRepository groupMemberReferRepository,
            UserReferRepository userReferRepository
    ) {
        this.groupReferRepository = groupReferRepository;
        this.groupMemberReferRepository = groupMemberReferRepository;
        this.userReferRepository = userReferRepository;
    }

    public ReferResult<GroupMemberEntity> invoke(GroupId groupId, UserId userId, GroupApproveFlag approveFlag, GroupLeaderFlag leaderFlag, Offset offset, Limit limit) {
        try {
            // 所属グループ未登録チェック
            List<GroupEntity> groupEntities = groupReferRepository.find(groupId, null, null, null, null);
            if (groupEntities.size() == 0) {
                return ReferResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.所属グループメンバー,
                        "group_id",
                        groupId.getValue()
                );
            }

            // ユーザー未登録チェック
            List<UserEntity> userEntities = userReferRepository.find(userId, null, null, null, null, null, null, null);
            if (userEntities.size() == 0) {
                return ReferResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.所属グループメンバー,
                        "user_id",
                        userId.getValue()
                );
            }

            return ReferResult.fromSuccess(
                    ItemNames.所属グループメンバー,
                    groupMemberReferRepository.find(groupId, userId, approveFlag, leaderFlag, offset, limit)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ReferResult.fromInternalServerError(ItemNames.所属グループメンバー);
        }
    }
}
