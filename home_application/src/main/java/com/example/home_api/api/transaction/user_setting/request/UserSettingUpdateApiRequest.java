package com.example.home_api.api.transaction.user_setting.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterRemarks;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class UserSettingUpdateApiRequest {
    UserId userId;
    UserSettingKey key;
    UserSettingValue value;

    public static ParamCheckResult paramCheck(JsonNode request) {

        ParamCheckResult paramCheckResultForUserId = ParamUtils.isValidCheckForRequired(request, "user_id");
        if (!paramCheckResultForUserId.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForUserId.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForKey = ParamUtils.isValidCheckForRequired(request, "key");
        if (!paramCheckResultForKey.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForKey.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForValue = ParamUtils.isValidCheckForOptional(request, "value");
        if (!paramCheckResultForValue.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForValue.getMessages()
            );
        }
        return ParamCheckResult.of(
                true,
                null
        );
    }

    public static UserSettingUpdateApiRequest from(JsonNode request) {

        return new UserSettingUpdateApiRequest(
                getParamForUserId(request),
                getParamForKey(request),
                getParamForValue(request)
        );
    }

    private static UserId getParamForUserId(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "user_id")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<user_id>");
                throw new IllegalArgumentException();
            default:
                return UserId.of(request.get("user_id").asText());
        }
    }

    private static UserSettingKey getParamForKey(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "key")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<key>");
                throw new IllegalArgumentException();
            default:
                return UserSettingKey.of(request.get("key").asText());
        }
    }

    private static UserSettingValue getParamForValue(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "value")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserSettingValue.of(request.get("value").asText());
        }
    }
}
