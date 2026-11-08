package com.example.home_api.domain.result;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import org.springframework.http.HttpStatus;

public class FixtureDeleteResult {

    public static DeleteResult 成功_通常(ItemNames itemNames) {
        return DeleteResult.fromSuccess(itemNames, 1);
    }

    public static DeleteResult 失敗_400_必須キー未指定(ItemNames itemNames, String keyName) {
        return DeleteResult.fromBadRequestErrorForMissingRequiredKey(itemNames, keyName);
    }

    public static DeleteResult 失敗_400_必須キー空欄(ItemNames itemNames, String keyName) {
        return DeleteResult.fromBadRequestErrorForRequiredKeyEmpty(itemNames, keyName);
    }

    public static DeleteResult 失敗_404_主キー未登録(ItemNames itemNames, String keyName, String value) {
        return DeleteResult.fromNotFoundErrorForRequiredKeyNotRegistered(itemNames, keyName,value);
    }

    public static DeleteResult 失敗_409_主キー重複(ItemNames itemNames, String keyName, String value) {
        return DeleteResult.fromConflictErrorForDuplicatePrimaryKey(itemNames, keyName,value);
    }

    public static DeleteResult 失敗_500(ItemNames itemNames) {
        return DeleteResult.fromInternalServerError(itemNames);
    }
}