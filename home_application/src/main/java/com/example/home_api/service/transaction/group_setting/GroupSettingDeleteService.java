package com.example.home_api.service.transaction.group_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingDeleteRepository;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingReferRepository;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.result.DeleteResult;

import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.service.master.group_setting_master.GroupSettingMasterReferService;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class GroupSettingDeleteService {

    GroupSettingReferRepository settingReferRepository;
    GroupSettingDeleteRepository settingDeleteRepository;
    GroupSettingMasterReferService settingMasterReferService;
    GroupReferRepository groupReferRepository;

    public GroupSettingDeleteService(GroupSettingReferRepository settingReferRepository,
                                     GroupSettingDeleteRepository settingDeleteRepository,
                                     GroupSettingMasterReferService settingMasterReferService,
                                     GroupReferRepository groupReferRepository) {
        this.settingReferRepository = settingReferRepository;
        this.settingDeleteRepository = settingDeleteRepository;
        this.settingMasterReferService = settingMasterReferService;
        this.groupReferRepository = groupReferRepository;
    }

    public DeleteResult invoke(GroupId groupId, GroupSettingKey key) {

        try {
            // ログ用にパラメータを文字列化
            String GroupIdString;
            if(groupId == null || groupId.getValue() == null){
                GroupIdString = "null";
            }else{
                GroupIdString = groupId.getValue();
            }
            String keyString;
            if(key == null || key.getValue() == null){
                keyString = "null";
            }else{
                keyString = key.getValue();
            }

            // 所属グループが存在しない
            if(groupReferRepository.find(groupId,null,null,null,null).isEmpty()){
                return DeleteResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.所属グループ設定,
                        "group_id",
                        groupId.getValue()
                );
            }

            if((groupId != null && groupId.getValue() != null) && (key != null && key.getValue() != null)){
                if (settingReferRepository.find(groupId, key).isEmpty()) {
                    return DeleteResult.fromNotFoundErrorForRelatedDataNotRegistered(
                            ItemNames.所属グループ設定,
                            "key/ Group_id",
                            String.join("/ ", List.of(GroupIdString, keyString))
                    );
                }
            }

            return DeleteResult.fromSuccess(
                    ItemNames.所属グループ設定,
                    settingDeleteRepository.delete(groupId, key)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return DeleteResult.fromInternalServerError(ItemNames.所属グループ設定);
        }
    }
}
