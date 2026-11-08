package com.example.home_api.service.transaction.group_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.GroupSettingMasterEntity;
import com.example.home_api.domain.entities.transaction.group.GroupSettingEntity;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingCreateRepository;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingReferRepository;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.result.ReferResult;

import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingValue;
import com.example.home_api.service.master.group_setting_master.GroupSettingMasterReferService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class GroupSettingCreateService {

    GroupSettingReferRepository settingReferRepository;
    GroupSettingCreateRepository settingCreateRepository;
    GroupSettingMasterReferService settingMasterReferService;
    GroupReferRepository groupReferRepository;

    public GroupSettingCreateService(GroupSettingReferRepository settingReferRepository,
                                     GroupSettingCreateRepository settingCreateRepository,
                                     GroupSettingMasterReferService settingMasterReferService,
                                     GroupReferRepository groupReferRepository) {
        this.settingReferRepository = settingReferRepository;
        this.settingCreateRepository = settingCreateRepository;
        this.settingMasterReferService = settingMasterReferService;
        this.groupReferRepository = groupReferRepository;
    }

    public CreateResult invoke(GroupId groupId, GroupSettingKey key, GroupSettingValue value) {

        try {
            // 所属グループが存在しない
            if(groupReferRepository.find(groupId,null,null,null,null).isEmpty()){
                return CreateResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.所属グループ設定,
                        "group_id",
                        groupId.getValue()
                );
            }

            // 所属グループ設定マスタ参照
            ReferResult<GroupSettingMasterEntity> groupSettingMasterReferResult;
            if(key == null || key.getValue() == null) {
                groupSettingMasterReferResult = settingMasterReferService.invoke(null);
            }else{
                groupSettingMasterReferResult = settingMasterReferService.invoke(GroupSettingMasterKey.of(key.getValue()));
            }

            // 所属グループ設定マスタの取得に失敗
            if (!groupSettingMasterReferResult.getHttpStatus().equals(HttpStatus.OK)) {
                return CreateResult.fromInternalServerErrorForRelatedDataRetrievalFailed(
                        ItemNames.所属グループ設定,
                        "key",
                        (key == null || key.getValue() == null) ? null : key.getValue()
                );
            }

            groupSettingMasterReferResult.getEntities().stream().map(entity -> {
                        List<GroupSettingEntity> groupSettingEntity = settingReferRepository.find(groupId, GroupSettingKey.of(entity.getKey().getValue()));
                        if (groupSettingEntity != null && groupSettingEntity.size() > 0) {
                            return null;
                        }
                        return settingCreateRepository.create(groupId, GroupSettingKey.of(entity.getKey().getValue()), GroupSettingValue.of(entity.getInitValue().getValue()));
                    })
                    .filter(Objects::nonNull)
                    .toList();

            return CreateResult.fromSuccess(
                    ItemNames.所属グループ設定,
                    null
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return CreateResult.fromInternalServerError(ItemNames.所属グループ設定);
        }
    }
}
