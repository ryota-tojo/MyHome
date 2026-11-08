package com.example.home_api.api.transaction.user.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class UserDeleteApiRequest {
    UserId userId;

    public static ParamCheckResult paramCheck(JsonNode request) {

        ParamCheckResult paramCheckResultForUserId = ParamUtils.isValidCheckForOptional(request, "user_id");
        if (!paramCheckResultForUserId.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForUserId.getMessages()
            );
        }
        return ParamCheckResult.of(
                true,
                null
        );
    }

    public static UserDeleteApiRequest from(JsonNode request) {

        return new UserDeleteApiRequest(
                getParamForUserId(request)
        );
    }

    private static UserId getParamForUserId(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "user_id")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return UserId.of(request.get("user_id").asText());
        }
    }
}
