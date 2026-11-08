package com.example.home_api.domain.entities;

import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.value_objects.*;

import java.util.List;

public class FixtureMasterSettingEntity {

    public static MasterSettingEntity 通常() {
        return MasterSettingEntity.of(
                FixtureSettingKey.通常(),
                FixtureSettingValue.通常(),
                FixtureSettingRemarks.通常(),
                FixtureCreateDatetime.通常(),
                FixtureUpdateDatetime.通常()
        );
    }

    public static List<MasterSettingEntity> リスト_通常() {
        return List.of(
                FixtureMasterSettingEntity.通常(),
                FixtureMasterSettingEntity.通常()
        );
    }
    public static List<MasterSettingEntity> リスト_空() {
        return List.of();
    }

}