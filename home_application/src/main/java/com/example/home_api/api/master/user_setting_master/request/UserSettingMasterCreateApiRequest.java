package com.example.home_api.api.master.user_setting_master.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterRemarks;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class UserSettingMasterCreateApiRequest {
    UserSettingMasterKey key;
    UserSettingMasterInitValue value;
    UserSettingMasterRemarks remarks;

    public static ParamCheckResult paramCheck(JsonNode request) {

        ParamCheckResult paramCheckResultForKey = ParamUtils.isValidCheckForRequired(request, "key");
        if (!paramCheckResultForKey.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForKey.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForValue = ParamUtils.isValidCheckForOptional(request, "init_value");
        if (!paramCheckResultForValue.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForValue.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForRemarks = ParamUtils.isValidCheckForOptional(request, "remarks");
        if (!paramCheckResultForRemarks.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForRemarks.getMessages()
            );
        }
        return ParamCheckResult.of(
                true,
                null
        );
    }

    public static UserSettingMasterCreateApiRequest from(JsonNode request) {

        return new UserSettingMasterCreateApiRequest(
                getParamForKey(request),
                getParamForValue(request),
                getParamForRemarks(request)
        );
    }

    private static UserSettingMasterKey getParamForKey(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "key")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<key>");
                throw new IllegalArgumentException();
            default:
                return UserSettingMasterKey.of(request.get("key").asText());
        }
    }

    private static UserSettingMasterInitValue getParamForValue(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "init_value")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserSettingMasterInitValue.of(request.get("init_value").asText());
        }
    }

    private static UserSettingMasterRemarks getParamForRemarks(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "remarks")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserSettingMasterRemarks.of(request.get("remarks").asText());
        }
    }
}
