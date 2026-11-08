package com.example.home_api.service.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingDeleteRepository;
import com.example.home_api.domain.result.DeleteResult;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import org.springframework.stereotype.Service;

@Service
public class MasterSettingDeleteService {

    private final MasterSettingDeleteRepository repository;
    private final MasterSettingReferService referService;

    public MasterSettingDeleteService(MasterSettingDeleteRepository repository, MasterSettingReferService referService) {
        this.repository = repository;
        this.referService = referService;
    }

    public DeleteResult invoke(MasterSettingKey key) {

        try {
            // キー登録チェック
            if (key != null && key.getValue() != null) {
                if (referService.invoke(key, null, null).getEntities().isEmpty()) {
                    return DeleteResult.fromNotFoundErrorForRequiredKeyNotRegistered(
                            ItemNames.マスタ設定,
                            "key",
                            key.getValue()
                    );
                }
            }

            // 削除処理実行
            return DeleteResult.fromSuccess(
                    ItemNames.マスタ設定,
                    repository.delete(key)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return DeleteResult.fromInternalServerError(ItemNames.マスタ設定);
        }
    }
}
