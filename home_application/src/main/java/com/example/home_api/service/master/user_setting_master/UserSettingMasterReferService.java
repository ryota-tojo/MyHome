package com.example.home_api.service.master.user_setting_master;


import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.UserSettingMasterEntity;
import com.example.home_api.domain.repositories.master.user_setting_master.UserSettingMasterReferRepository;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import org.springframework.stereotype.Service;

@Service
public class UserSettingMasterReferService {

    private final UserSettingMasterReferRepository repository;

    public UserSettingMasterReferService(UserSettingMasterReferRepository repository) {
        this.repository = repository;
    }

    public ReferResult<UserSettingMasterEntity> invoke(UserSettingMasterKey key) {
        try {
            return ReferResult.fromSuccess(
                    ItemNames.ユーザー設定マスタ,
                    repository.find(key)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ReferResult.fromInternalServerError(ItemNames.ユーザー設定マスタ);
        }
    }
}
