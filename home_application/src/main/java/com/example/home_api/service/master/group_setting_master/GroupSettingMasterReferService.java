package com.example.home_api.service.master.group_setting_master;


import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.GroupSettingMasterEntity;
import com.example.home_api.domain.repositories.master.group_setting_master.GroupSettingMasterReferRepository;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import org.springframework.stereotype.Service;

@Service
public class GroupSettingMasterReferService {

    private final GroupSettingMasterReferRepository repository;

    public GroupSettingMasterReferService(GroupSettingMasterReferRepository repository) {
        this.repository = repository;
    }

    public ReferResult<GroupSettingMasterEntity> invoke(GroupSettingMasterKey key) {
        try {
            return ReferResult.fromSuccess(
                    ItemNames.所属グループ設定マスタ,
                    repository.find(key)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ReferResult.fromInternalServerError(ItemNames.所属グループ設定マスタ);
        }
    }
}
