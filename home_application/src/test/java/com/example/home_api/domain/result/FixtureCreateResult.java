package com.example.home_api.domain.result;

import com.example.home_api.domain.common.ItemNames;

public class FixtureCreateResult {

    public static CreateResult<String> 成功_通常(ItemNames itemNames, String primaryKey) {
        return CreateResult.fromSuccess(itemNames, primaryKey);
    }

    public static CreateResult<String> 失敗_400_必須キー未指定(ItemNames itemNames, String keyName) {
        return CreateResult.fromBadRequestErrorForMissingRequiredKey(itemNames, keyName);
    }

    public static CreateResult<String> 失敗_400_必須キー空欄(ItemNames itemNames, String keyName) {
        return CreateResult.fromBadRequestErrorForRequiredKeyEmpty(itemNames, keyName);
    }

    public static CreateResult<String> 失敗_404_主キー未登録(ItemNames itemNames, String keyName, String value) {
        return CreateResult.fromNotFoundErrorForRequiredKeyNotRegistered(itemNames, keyName, value);
    }

    public static CreateResult<String> 失敗_409_主キー重複(ItemNames itemNames, String keyName, String value) {
        return CreateResult.fromConflictErrorForDuplicatePrimaryKey(itemNames, keyName, value);
    }

    public static CreateResult<String> 失敗_500(ItemNames itemNames) {
        return CreateResult.fromInternalServerError(itemNames);
    }
}