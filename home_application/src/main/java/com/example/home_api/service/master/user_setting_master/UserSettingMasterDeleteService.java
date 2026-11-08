package com.example.home_api.service.master.user_setting_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.master.user_setting_master.UserSettingMasterDeleteRepository;
import com.example.home_api.domain.result.DeleteResult;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import org.springframework.stereotype.Service;

@Service
public class UserSettingMasterDeleteService {

    UserSettingMasterDeleteRepository repository;
    UserSettingMasterReferService referService;

    public UserSettingMasterDeleteService(UserSettingMasterDeleteRepository repository, UserSettingMasterReferService referService) {
        this.repository = repository;
        this.referService = referService;
    }

    public DeleteResult invoke(UserSettingMasterKey key) {

        try {
            if (key != null && key.getValue() != null) {
                if (referService.invoke(key).getEntities().isEmpty()) {
                    return DeleteResult.fromNotFoundErrorForRequiredKeyNotRegistered(
                            ItemNames.ユーザー設定マスタ,
                            "key",
                            key.getValue()
                    );
                }
            }

            return DeleteResult.fromSuccess(
                    ItemNames.ユーザー設定マスタ,
                    repository.delete(key)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return DeleteResult.fromInternalServerError(ItemNames.ユーザー設定マスタ);
        }
    }
}
