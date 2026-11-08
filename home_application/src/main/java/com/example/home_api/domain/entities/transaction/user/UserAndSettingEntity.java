package com.example.home_api.domain.entities.transaction.user;

import lombok.Value;

import java.util.List;

@Value
public class UserAndSettingEntity {
    UserEntity userEntity;
    List<UserSettingEntity> settingEntities;

    public static UserAndSettingEntity of(
            UserEntity userEntity,
            List<UserSettingEntity> settingEntities
    ) {
        return new UserAndSettingEntity(
                userEntity,
                settingEntities
        );
    }
}
