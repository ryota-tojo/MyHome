package com.example.home_api.service.transaction.user_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingReferRepository;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingUpdateRepository;
import com.example.home_api.domain.result.UpdateResult;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingValue;
import com.example.home_api.service.master.user_setting_master.UserSettingMasterReferService;
import com.example.home_api.service.transaction.user.UserReferService;
import org.springframework.stereotype.Service;

@Service
public class UserSettingUpdateService {

    UserSettingReferRepository settingReferRepository;
    UserSettingUpdateRepository settingUpdateRepository;
    UserSettingMasterReferService settingMasterReferService;
    UserReferService userReferService;

    public UserSettingUpdateService(UserSettingReferRepository settingReferRepository,
                                    UserSettingUpdateRepository settingUpdateRepository,
                                    UserSettingMasterReferService settingMasterReferService,
                                    UserReferService userReferService) {
        this.settingReferRepository = settingReferRepository;
        this.settingUpdateRepository = settingUpdateRepository;
        this.settingMasterReferService = settingMasterReferService;
        this.userReferService = userReferService;
    }

    public UpdateResult invoke(UserId userId, UserSettingKey key, UserSettingValue value) {

        try {
            // ユーザーが存在しない
            if (userReferService.invoke(userId, null, null, null, null, null, null, null).getEntities().size() == 0) {
                return UpdateResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.ユーザー設定,
                        "user_id",
                        userId.getValue()
                );
            }

            // ユーザー設定参照：存在しない
            if (settingReferRepository.find(userId, key).isEmpty()) {
                return UpdateResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.ユーザー設定,
                        key.getValue(),
                        value.getValue()
                );
            }

            return UpdateResult.fromSuccess(
                    ItemNames.ユーザー設定,
                    settingUpdateRepository.update(userId, key, value)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return UpdateResult.fromInternalServerError(ItemNames.ユーザー設定);
        }
    }
}
