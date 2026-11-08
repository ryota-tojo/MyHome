package com.example.home_api.service.transaction.group_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingReferRepository;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingUpdateRepository;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.result.UpdateResult;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingValue;
import com.example.home_api.service.master.group_setting_master.GroupSettingMasterReferService;
import org.springframework.stereotype.Service;

@Service
public class GroupSettingUpdateService {

    GroupSettingReferRepository settingReferRepository;
    GroupSettingUpdateRepository settingUpdateRepository;
    GroupSettingMasterReferService settingMasterReferService;
    GroupReferRepository groupReferRepository;

    public GroupSettingUpdateService(GroupSettingReferRepository settingReferRepository,
                                     GroupSettingUpdateRepository settingUpdateRepository,
                                     GroupSettingMasterReferService settingMasterReferService,
                                     GroupReferRepository groupReferRepository) {
        this.settingReferRepository = settingReferRepository;
        this.settingUpdateRepository = settingUpdateRepository;
        this.settingMasterReferService = settingMasterReferService;
        this.groupReferRepository = groupReferRepository;
    }

    public UpdateResult invoke(GroupId groupId, GroupSettingKey key, GroupSettingValue value) {

        try {
            // 所属グループが存在しない
            if (groupReferRepository.find(groupId, null, null, null, null).isEmpty()) {
                return UpdateResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.所属グループ設定,
                        "group_id",
                        groupId.getValue()
                );
            }

            // 所属グループ設定参照：存在しない
            if (settingReferRepository.find(groupId, key).isEmpty()) {
                return UpdateResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.所属グループ設定,
                        key.getValue(),
                        value.getValue()
                );
            }

            return UpdateResult.fromSuccess(
                    ItemNames.所属グループ設定,
                    settingUpdateRepository.update(groupId, key, value)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return UpdateResult.fromInternalServerError(ItemNames.所属グループ設定);
        }
    }
}
