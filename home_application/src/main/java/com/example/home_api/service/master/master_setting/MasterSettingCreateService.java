package com.example.home_api.service.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingCreateRepository;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;
import org.springframework.stereotype.Service;

@Service
public class MasterSettingCreateService {

    private final MasterSettingCreateRepository repository;
    private final MasterSettingReferService referService;

    public MasterSettingCreateService(MasterSettingCreateRepository repository, MasterSettingReferService referService) {
        this.repository = repository;
        this.referService = referService;
    }

    public CreateResult<String> invoke(MasterSettingKey key, MasterSettingValue value, MasterSettingRemarks remarks) {

        try {

            // キー重複チェック
            if (key != null && key.getValue() != null) {
                if (!referService.invoke(key, null, null).getEntities().isEmpty()) {
                    return CreateResult.fromConflictErrorForDuplicatePrimaryKey(
                            ItemNames.マスタ設定,
                            "key",
                            key.getValue()
                    );
                }
            }

            // 作成処理実行
            return CreateResult.fromSuccess(
                    ItemNames.マスタ設定,
                    MasterSettingKey.of(repository.create(key, value, remarks)).getValue()
            );

        } catch (Exception e) {
            return CreateResult.fromInternalServerError(ItemNames.マスタ設定);
        }
    }

}
