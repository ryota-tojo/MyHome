package com.example.home_api.service.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingUpdateRepository;
import com.example.home_api.domain.result.UpdateResult;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;
import org.springframework.stereotype.Service;

@Service
public class MasterSettingUpdateService {

    private final MasterSettingUpdateRepository repository;
    private final MasterSettingReferService referService;

    public MasterSettingUpdateService(MasterSettingUpdateRepository repository, MasterSettingReferService referService) {
        this.repository = repository;
        this.referService = referService;
    }

    public UpdateResult invoke(MasterSettingKey key, MasterSettingValue value, MasterSettingRemarks remarks) {

        try {
            // キー登録チェック
            if (referService.invoke(key, null, null).getEntities().isEmpty()) {
                return UpdateResult.fromNotFoundErrorForRequiredKeyNotRegistered(
                        ItemNames.マスタ設定,
                        "key",
                        key.getValue()
                );
            }

            // 更新処理実行
            return UpdateResult.fromSuccess(
                    ItemNames.マスタ設定,
                    repository.update(key, value, remarks)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return UpdateResult.fromInternalServerError(ItemNames.マスタ設定);
        }
    }
}
