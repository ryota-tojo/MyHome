package com.example.home_api.service.transaction.user_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.UserSettingMasterEntity;
import com.example.home_api.domain.entities.transaction.user.UserSettingEntity;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingCreateRepository;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingReferRepository;
import com.example.home_api.domain.repositories.transaction.user.UserReferRepository;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingValue;
import com.example.home_api.service.master.user_setting_master.UserSettingMasterReferService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserSettingCreateService {

    UserSettingReferRepository settingReferRepository;
    UserSettingCreateRepository settingCreateRepository;
    UserSettingMasterReferService settingMasterReferService;
    UserReferRepository userReferRepository;

    public UserSettingCreateService(UserSettingReferRepository settingReferRepository,
                                    UserSettingCreateRepository settingCreateRepository,
                                    UserSettingMasterReferService settingMasterReferService,
                                    UserReferRepository userReferRepository) {
        this.settingReferRepository = settingReferRepository;
        this.settingCreateRepository = settingCreateRepository;
        this.settingMasterReferService = settingMasterReferService;
        this.userReferRepository = userReferRepository;
    }

    public CreateResult invoke(UserId userId, UserSettingKey key, UserSettingValue value) {

        try {

            // ユーザーが存在しない
            if(userReferRepository.find(userId,null,null,null,null,null,null,null).isEmpty()){
                return CreateResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.ユーザー設定,
                        "user_id",
                        userId.getValue()
                );
            }

            // ユーザー設定マスタ参照
            ReferResult<UserSettingMasterEntity> userSettingMasterReferResult;
            if(key == null || key.getValue() == null) {
                userSettingMasterReferResult = settingMasterReferService.invoke(null);
            }else{
                userSettingMasterReferResult = settingMasterReferService.invoke(UserSettingMasterKey.of(key.getValue()));
            }

            // ユーザー設定マスタの取得に失敗
            if (!userSettingMasterReferResult.getHttpStatus().equals(HttpStatus.OK)) {
                return CreateResult.fromInternalServerErrorForRelatedDataRetrievalFailed(
                        ItemNames.ユーザー設定,
                        "key",
                        (key == null || key.getValue() == null) ? null : key.getValue()
                );
            }

            userSettingMasterReferResult.getEntities().stream().map(entity -> {
                        List<UserSettingEntity> userSettingEntity = settingReferRepository.find(userId, UserSettingKey.of(entity.getKey().getValue()));
                        if (userSettingEntity != null && userSettingEntity.size() > 0) {
                            return null;
                        }
                        return settingCreateRepository.create(userId, UserSettingKey.of(entity.getKey().getValue()), UserSettingValue.of(entity.getInitValue().getValue()));
                    })
                    .filter(Objects::nonNull)
                    .toList();

            return CreateResult.fromSuccess(
                    ItemNames.ユーザー設定,
                    null
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return CreateResult.fromInternalServerError(ItemNames.ユーザー設定);
        }
    }
}
