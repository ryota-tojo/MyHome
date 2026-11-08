package com.example.home_api.service.transaction.group;

import com.example.home_api.domain.common.ItemNames;

import com.example.home_api.domain.entities.transaction.group.GroupEntity;
import com.example.home_api.domain.repositories.transaction.group.GroupCreateRepository;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group.GroupName;
import com.example.home_api.domain.value_objects.transaction.group.GroupPassword;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupApproveFlag;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.service.transaction.group_setting.GroupSettingCreateService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupCreateService {

    GroupReferRepository groupReferRepository;
    GroupCreateRepository groupCreateRepository;
    GroupSettingCreateService groupSettingCreateService;

    public GroupCreateService(GroupReferRepository groupReferRepository,
                              GroupCreateRepository groupCreateRepository,
                              GroupSettingCreateService groupSettingCreateService
                             ) {
        this.groupReferRepository = groupReferRepository;
        this.groupCreateRepository = groupCreateRepository;
        this.groupSettingCreateService  = groupSettingCreateService;
    }

    public CreateResult invoke(UserId createUserId, GroupId groupId, GroupPassword password, GroupName name) {

        try {
            // 所属グループ重複チェック
            List<GroupEntity> entities = groupReferRepository.find(groupId, null,null,null,null);
            if (entities.size() > 0) {
                return CreateResult.fromConflictErrorForDuplicatePrimaryKey(
                        ItemNames.所属グループ,
                        "group_id",
                        groupId.getValue()
                );
            }

            // 所属グループ作成
            GroupId createGroupId = GroupId.of(groupCreateRepository.create(groupId, password, name));

            // 所属グループ設定作成
            if(!groupSettingCreateService.invoke(createGroupId,null,null).getHttpStatus().equals(HttpStatus.OK)){
                return CreateResult.fromInternalServerError(ItemNames.所属グループ設定);
            }

            // TODO 所属グループメンバー作成（リーダー）


            // TODO 初期カテゴリー作成


            // TODO 初期購入ラベル作成


            return CreateResult.fromSuccess(
                    ItemNames.所属グループ,
                    createGroupId.getValue()
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return CreateResult.fromInternalServerError(ItemNames.所属グループ);
        }
    }
}
