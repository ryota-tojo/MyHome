package com.example.home_api.domain.result;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import org.springframework.http.HttpStatus;

public class FixtureUpdateResult {

    public static UpdateResult 成功_通常(ItemNames itemNames) {
        return UpdateResult.fromSuccess(itemNames, 1);
    }

    public static UpdateResult 失敗_400_必須キー未指定(ItemNames itemNames, String keyName) {
        return UpdateResult.fromBadRequestErrorForMissingRequiredKey(itemNames, keyName);
    }

    public static UpdateResult 失敗_400_必須キー空欄(ItemNames itemNames, String keyName) {
        return UpdateResult.fromBadRequestErrorForRequiredKeyEmpty(itemNames,  keyName);
    }

    public static UpdateResult 失敗_404_主キー未登録(ItemNames itemNames, String keyName, String value) {
        return UpdateResult.fromNotFoundErrorForRequiredKeyNotRegistered(itemNames, keyName, value);
    }

    public static UpdateResult 失敗_409_主キー重複(ItemNames itemNames, String keyName, String value) {
        return UpdateResult.fromConflictErrorForDuplicatePrimaryKey(itemNames, keyName, value);
    }

    public static UpdateResult 失敗_500(ItemNames itemNames) {
        return UpdateResult.fromInternalServerError(itemNames);
    }
}