package com.example.home_api.api.master.user_setting_master.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class UserSettingMasterDeleteApiRequest {
    UserSettingMasterKey key;

    public static ParamCheckResult paramCheck(JsonNode request) {

        ParamCheckResult paramCheckResultForKey = ParamUtils.isValidCheckForOptional(request, "key");
        if (!paramCheckResultForKey.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForKey.getMessages()
            );
        }
        return ParamCheckResult.of(
                true,
                null
        );
    }

    public static UserSettingMasterDeleteApiRequest from(JsonNode request) {

        return new UserSettingMasterDeleteApiRequest(
                getParamForKey(request)
        );
    }

    private static UserSettingMasterKey getParamForKey(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "key")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserSettingMasterKey.of(request.get("key").asText());
        }
    }
}
