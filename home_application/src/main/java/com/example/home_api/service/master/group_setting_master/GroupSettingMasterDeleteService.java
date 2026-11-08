package com.example.home_api.service.master.group_setting_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.master.group_setting_master.GroupSettingMasterDeleteRepository;
import com.example.home_api.domain.result.DeleteResult;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import org.springframework.stereotype.Service;

@Service
public class GroupSettingMasterDeleteService {

    private final GroupSettingMasterDeleteRepository repository;
    private final GroupSettingMasterReferService referService;

    public GroupSettingMasterDeleteService(GroupSettingMasterDeleteRepository repository, GroupSettingMasterReferService referService) {
        this.repository = repository;
        this.referService = referService;
    }

    public DeleteResult invoke(GroupSettingMasterKey key) {

        try {
            // キー登録チェック
            if (key != null && key.getValue() != null) {
                if (referService.invoke(key).getEntities().isEmpty()) {
                    return DeleteResult.fromNotFoundErrorForRequiredKeyNotRegistered(
                            ItemNames.所属グループ設定マスタ,
                            "key",
                            key.getValue()
                    );
                }
            }

            // 削除処理実行
            return DeleteResult.fromSuccess(
                    ItemNames.所属グループ設定マスタ,
                    repository.delete(key)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return DeleteResult.fromInternalServerError(ItemNames.所属グループ設定マスタ);
        }
    }
}
