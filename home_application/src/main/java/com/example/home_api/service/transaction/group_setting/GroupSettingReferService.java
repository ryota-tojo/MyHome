package com.example.home_api.service.transaction.group_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.transaction.group.GroupSettingEntity;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingReferRepository;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.service.master.group_setting_master.GroupSettingMasterReferService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupSettingReferService {

    GroupSettingReferRepository settingReferRepository;
    GroupSettingMasterReferService settingMasterReferService;
    GroupReferRepository groupReferRepository;

    public GroupSettingReferService(GroupSettingReferRepository settingReferRepository,
                                    GroupSettingMasterReferService settingMasterReferService,
                                    GroupReferRepository groupReferRepository) {
        this.settingReferRepository = settingReferRepository;
        this.settingMasterReferService = settingMasterReferService;
        this.groupReferRepository = groupReferRepository;
    }

    public ReferResult<GroupSettingEntity> invoke(GroupId groupId, GroupSettingKey key) {
        try {
            // 所属グループが存在しない
            if (groupReferRepository.find(groupId, null, null, null, null).isEmpty()) {
                return ReferResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.所属グループ設定,
                        "group_id",
                        groupId.getValue()
                );
            }

            List<GroupSettingEntity> entity = settingReferRepository.find(groupId, key);
            return ReferResult.fromSuccess(ItemNames.所属グループ設定, entity);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ReferResult.fromInternalServerError(ItemNames.所属グループ設定);
        }
    }
}
