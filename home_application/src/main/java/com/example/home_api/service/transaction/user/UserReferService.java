package com.example.home_api.service.transaction.user;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.entities.transaction.user.UserAndSettingEntity;
import com.example.home_api.domain.entities.transaction.user.UserEntity;
import com.example.home_api.domain.entities.transaction.user.UserSettingEntity;
import com.example.home_api.domain.repositories.transaction.user.UserReferRepository;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.transaction.user.*;
import com.example.home_api.service.transaction.user_setting.UserSettingReferService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserReferService {

    UserReferRepository repository;
    UserSettingReferService settingReferService;

    public UserReferService(UserReferRepository repository,UserSettingReferService settingReferService) {
        this.repository = repository;
        this.settingReferService = settingReferService;
    }

    public ReferResult<UserAndSettingEntity> invoke(UserId userId, UserPassword userPassword, UserName name, UserPermissionFlag permissionflag, UserApproveFlag approveflag, UserDeleteFlag deleteflag, Offset offset, Limit limit) {
        try {

            List<UserEntity> userEntity = repository.find(userId, userPassword, name, permissionflag, approveflag, deleteflag, offset, limit);
            List<UserAndSettingEntity> entities = userEntity.stream().map(entity -> {
                List<UserSettingEntity> settingEntities = settingReferService.invoke(entity.getUserId(), null).getEntities();
                return UserAndSettingEntity.of(entity, settingEntities);
            }).toList();

            return ReferResult.fromSuccess(ItemNames.ユーザー, entities);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ReferResult.fromInternalServerError(ItemNames.ユーザー);
        }
    }
}
