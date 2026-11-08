package com.example.home_api.service.master.user_setting_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.master.user_setting_master.UserSettingMasterUpdateRepository;
import com.example.home_api.domain.result.UpdateResult;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterRemarks;
import org.springframework.stereotype.Service;

@Service
public class UserSettingMasterUpdateService {

    UserSettingMasterUpdateRepository repository;
    UserSettingMasterReferService referService;

    public UserSettingMasterUpdateService(UserSettingMasterUpdateRepository repository, UserSettingMasterReferService referService) {
        this.repository = repository;
        this.referService = referService;
    }

    public UpdateResult invoke(UserSettingMasterKey key, UserSettingMasterInitValue value, UserSettingMasterRemarks remarks) {

        try {
            if (referService.invoke(key).getEntities().isEmpty()) {
                return UpdateResult.fromNotFoundErrorForRequiredKeyNotRegistered(
                        ItemNames.ユーザー設定マスタ,
                        "key",
                        key.getValue()
                );
            }

            return UpdateResult.fromSuccess(
                    ItemNames.ユーザー設定マスタ,
                    repository.update(key, value, remarks)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return UpdateResult.fromInternalServerError(ItemNames.ユーザー設定マスタ);
        }
    }
}
