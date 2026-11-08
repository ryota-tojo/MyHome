package com.example.home_api.service.transaction.user;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.transaction.user.UserEntity;
import com.example.home_api.domain.repositories.transaction.user.UserReferRepository;
import com.example.home_api.domain.repositories.transaction.user.UserUpdateRepository;
import com.example.home_api.domain.result.UpdateResult;
import com.example.home_api.domain.value_objects.transaction.user.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserUpdateService {

    UserReferRepository referRepository;
    UserUpdateRepository updateRepository;

    public UserUpdateService(UserReferRepository referRepository,
                             UserUpdateRepository updateRepository) {
        this.referRepository = referRepository;
        this.updateRepository = updateRepository;
    }

    public UpdateResult invoke(UserId userId, UserPassword password, UserName name, UserPermissionFlag permissionFlag, UserApproveFlag approveFlag, UserDeleteFlag deleteFlag) {
        // ユーザー未登録チェック
        List<UserEntity> entities =  referRepository.find(userId,null,null,null,null,null,null,null);
        if(entities.size() == 0){
            return UpdateResult.fromNotFoundErrorForRelatedDataNotRegistered(
                    ItemNames.ユーザー,
                    "user_id",
                    userId.getValue()
            );
        }
        try {


            return UpdateResult.fromSuccess(
                    ItemNames.ユーザー,
                    updateRepository.update(userId, password, name,permissionFlag,approveFlag,deleteFlag)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return UpdateResult.fromInternalServerError(ItemNames.ユーザー);
        }
    }
}
