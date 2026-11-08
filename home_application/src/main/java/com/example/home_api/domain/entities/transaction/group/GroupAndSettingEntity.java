package com.example.home_api.domain.entities.transaction.group;

import lombok.Value;

import java.util.List;

@Value
public class GroupAndSettingEntity {
    GroupEntity groupEntity;
    List<GroupSettingEntity> settingEntities;

    public static GroupAndSettingEntity of(
            GroupEntity groupEntity,
            List<GroupSettingEntity> settingEntities
    ) {
        return new GroupAndSettingEntity(
                groupEntity,
                settingEntities
        );
    }
}
