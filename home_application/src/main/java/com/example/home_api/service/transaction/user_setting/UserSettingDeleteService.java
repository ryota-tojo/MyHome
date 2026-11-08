package com.example.home_api.service.transaction.user_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingDeleteRepository;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingReferRepository;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.result.DeleteResult;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.service.master.user_setting_master.UserSettingMasterReferService;
import com.example.home_api.service.transaction.user.UserReferService;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class UserSettingDeleteService {

    UserSettingReferRepository settingReferRepository;
    UserSettingDeleteRepository settingDeleteRepository;
    UserSettingMasterReferService settingMasterReferService;
    UserReferService userReferService;

    public UserSettingDeleteService(UserSettingReferRepository settingReferRepository,
                                    UserSettingDeleteRepository settingDeleteRepository,
                                    UserSettingMasterReferService settingMasterReferService,
                                    UserReferService userReferService) {
        this.settingReferRepository = settingReferRepository;
        this.settingDeleteRepository = settingDeleteRepository;
        this.settingMasterReferService = settingMasterReferService;
        this.userReferService = userReferService;
    }

    public DeleteResult invoke(UserId userId, UserSettingKey key) {

        try {
            // ログ用にパラメータを文字列化
            String userIdString;
            if(userId == null || userId.getValue() == null){
                userIdString = "null";
            }else{
                userIdString = userId.getValue();
            }
            String keyString;
            if(key == null || key.getValue() == null){
                keyString = "null";
            }else{
                keyString = key.getValue();
            }

            // ユーザーが存在しない
            if(userReferService.invoke(userId,null,null,null,null,null,null,null).getEntities().size() == 0){
                return DeleteResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.ユーザー設定,
                        "user_id",
                        userId.getValue()
                );
            }

            if((userId != null && userId.getValue() != null) && (key != null && key.getValue() != null)){
                if (settingReferRepository.find(userId, key).isEmpty()) {
                    return DeleteResult.fromNotFoundErrorForRelatedDataNotRegistered(
                            ItemNames.ユーザー設定,
                            "key/ user_id",
                            String.join("/ ", List.of(userIdString, keyString))
                    );
                }
            }

            return DeleteResult.fromSuccess(
                    ItemNames.ユーザー設定,
                    settingDeleteRepository.delete(userId, key)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return DeleteResult.fromInternalServerError(ItemNames.ユーザー設定);
        }
    }
}
