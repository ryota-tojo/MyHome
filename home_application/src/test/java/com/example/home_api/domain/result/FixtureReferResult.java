package com.example.home_api.domain.result;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.FixtureMasterSettingEntity;
import com.example.home_api.domain.entities.master.MasterSettingEntity;

public class FixtureReferResult {

    public static ReferResult<MasterSettingEntity> マスタ設定_成功_通常() {
        return ReferResult.fromSuccess(
                ItemNames.マスタ設定,
                FixtureMasterSettingEntity.リスト_通常()
        );
    }

    public static ReferResult<MasterSettingEntity> マスタ設定_成功_空のリスト() {
        return ReferResult.fromSuccess(
                ItemNames.マスタ設定,
                FixtureMasterSettingEntity.リスト_空()
        );
    }

    public static ReferResult<MasterSettingEntity> マスタ設定_失敗_想定外エラー() {
        return ReferResult.fromInternalServerError(ItemNames.マスタ設定);
    }
}