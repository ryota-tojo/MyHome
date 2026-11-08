package com.example.home_api.service.transaction.user;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.transaction.user.UserEntity;
import com.example.home_api.domain.repositories.transaction.user.UserDeleteRepository;
import com.example.home_api.domain.repositories.transaction.user.UserReferRepository;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.result.DeleteResult;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.service.transaction.user_setting.UserSettingCreateService;
import com.example.home_api.service.transaction.user_setting.UserSettingDeleteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class UserDeleteService {

    UserReferRepository referRepository;
    UserDeleteRepository deleteRepository;
    UserSettingDeleteService userSettingDeleteService;

    public UserDeleteService(UserReferRepository referRepository,
                             UserDeleteRepository deleteRepository,
                             UserSettingDeleteService userSettingDeleteService
                             ) {
        this.referRepository = referRepository;
        this.deleteRepository = deleteRepository;
        this.userSettingDeleteService = userSettingDeleteService;
    }

    public DeleteResult invoke(UserId userId) {

        try {
            // ユーザー未登録チェック
            List<UserEntity> entities =  referRepository.find(userId,null,null,null,null,null,null,null);
            if(entities.size() == 0){
                return DeleteResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.ユーザー,
                        "user_id",
                        userId.getValue()
                );
            }

            // TODO 所属グループのリーダーの場合、削除不可

            // TODO 所属グループメンバー削除

            // ユーザー設定削除
            if(!userSettingDeleteService.invoke(userId, null).getHttpStatus().equals(HttpStatus.OK)){
                return DeleteResult.fromInternalServerError(ItemNames.ユーザー設定);
            }

            return DeleteResult.fromSuccess(
                    ItemNames.ユーザー,
                    deleteRepository.delete(userId)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return DeleteResult.fromInternalServerError(ItemNames.ユーザー);
        }
    }
}
