package com.example.home_api.service.transaction.user_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.transaction.user.UserSettingEntity;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingReferRepository;
import com.example.home_api.domain.repositories.transaction.user.UserReferRepository;
import com.example.home_api.domain.result.DeleteResult;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.service.master.user_setting_master.UserSettingMasterReferService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSettingReferService {

    UserSettingReferRepository settingReferRepository;
    UserSettingMasterReferService settingMasterReferService;
    UserReferRepository userReferRepository;

    public UserSettingReferService(UserSettingReferRepository settingReferRepository,
                                   UserSettingMasterReferService settingMasterReferService,
                                   UserReferRepository userReferRepository) {
        this.settingReferRepository = settingReferRepository;
        this.settingMasterReferService = settingMasterReferService;
        this.userReferRepository = userReferRepository;
    }

    public ReferResult<UserSettingEntity> invoke(UserId userId, UserSettingKey key) {
        try {
            // ユーザーが存在しない
            if(userReferRepository.find(userId,null,null,null,null,null,null,null).isEmpty()){
                return ReferResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.ユーザー設定,
                        "user_id",
                        userId.getValue()
                );
            }

            List<UserSettingEntity> entity =  settingReferRepository.find(userId, key);
            return ReferResult.fromSuccess(ItemNames.ユーザー設定,entity);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ReferResult.fromInternalServerError(ItemNames.ユーザー設定);
        }
    }
}
