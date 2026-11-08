package com.example.home_api.service.master.group_setting_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.master.group_setting_master.GroupSettingMasterUpdateRepository;
import com.example.home_api.domain.result.UpdateResult;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterRemarks;
import org.springframework.stereotype.Service;

@Service
public class GroupSettingMasterUpdateService {

    private final GroupSettingMasterUpdateRepository repository;
    private final GroupSettingMasterReferService referService;

    public GroupSettingMasterUpdateService(GroupSettingMasterUpdateRepository repository, GroupSettingMasterReferService referService) {
        this.repository = repository;
        this.referService = referService;
    }

    public UpdateResult invoke(GroupSettingMasterKey key, GroupSettingMasterInitValue value, GroupSettingMasterRemarks remarks) {

        try {
            // キー登録チェック
            if (referService.invoke(key).getEntities().isEmpty()) {
                return UpdateResult.fromNotFoundErrorForRequiredKeyNotRegistered(
                        ItemNames.所属グループ設定マスタ,
                        "key",
                        key.getValue()
                );
            }

            // 更新処理実行
            return UpdateResult.fromSuccess(
                    ItemNames.所属グループ設定マスタ,
                    repository.update(key, value, remarks)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return UpdateResult.fromInternalServerError(ItemNames.所属グループ設定マスタ);
        }
    }
}
