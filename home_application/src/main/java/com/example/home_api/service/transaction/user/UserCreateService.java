package com.example.home_api.service.transaction.user;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.transaction.user.UserEntity;
import com.example.home_api.domain.repositories.transaction.user.UserCreateRepository;
import com.example.home_api.domain.repositories.transaction.user.UserReferRepository;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.value_objects.transaction.user.*;
import com.example.home_api.service.transaction.user_setting.UserSettingCreateService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCreateService {

    UserReferRepository userReferRepository;
    UserCreateRepository userCreateRepository;
    UserSettingCreateService userSettingCreateService;

    public UserCreateService(UserReferRepository userReferRepository,
                             UserCreateRepository userCreateRepository,
                             UserSettingCreateService userSettingCreateService
                             ) {
        this.userReferRepository = userReferRepository;
        this.userCreateRepository = userCreateRepository;
        this.userSettingCreateService  = userSettingCreateService;
    }

    public CreateResult invoke(UserId userId, UserPassword password, UserName name, UserPermissionFlag permissionflag, UserApproveFlag approveflag, UserDeleteFlag deleteflag) {

        try {
            // ユーザー重複チェック
            List<UserEntity> entities = userReferRepository.find(userId, null, null, null, null, null, null, null);
            if (entities.size() > 0) {
                return CreateResult.fromConflictErrorForDuplicatePrimaryKey(
                        ItemNames.ユーザー,
                        "user_id",
                        userId.getValue()
                );
            }

            // ユーザー作成
            UserId createUserId = UserId.of(userCreateRepository.create(userId, password, name,permissionflag,approveflag,deleteflag));

            // ユーザー設定作成
            if(!userSettingCreateService.invoke(createUserId,null,null).getHttpStatus().equals(HttpStatus.OK)){
                return CreateResult.fromInternalServerError(ItemNames.ユーザー設定);
            }

            return CreateResult.fromSuccess(
                    ItemNames.ユーザー,
                    createUserId.getValue()
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return CreateResult.fromInternalServerError(ItemNames.ユーザー);
        }
    }
}
