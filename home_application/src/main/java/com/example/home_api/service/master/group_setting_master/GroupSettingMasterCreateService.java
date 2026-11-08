package com.example.home_api.service.master.group_setting_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.master.group_setting_master.GroupSettingMasterCreateRepository;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterRemarks;
import org.springframework.stereotype.Service;

@Service
public class GroupSettingMasterCreateService {

    private final GroupSettingMasterCreateRepository repository;
    private final GroupSettingMasterReferService referService;

    public GroupSettingMasterCreateService(GroupSettingMasterCreateRepository repository, GroupSettingMasterReferService referService) {
        this.repository = repository;
        this.referService = referService;
    }

    public CreateResult<String> invoke(GroupSettingMasterKey key, GroupSettingMasterInitValue value, GroupSettingMasterRemarks remarks) {

        try {
            // キー重複チェック
            if (!referService.invoke(key).getEntities().isEmpty()) {
                return CreateResult.fromConflictErrorForDuplicatePrimaryKey(
                        ItemNames.所属グループ設定マスタ,
                        "key",
                        key.getValue()
                );
            }

            // 作成処理実行
            return CreateResult.fromSuccess(
                    ItemNames.所属グループ設定マスタ,
                    GroupSettingMasterKey.of(repository.create(key, value, remarks)).getValue()
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return CreateResult.fromInternalServerError(ItemNames.所属グループ設定マスタ);
        }
    }
}
