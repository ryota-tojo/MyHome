package com.example.home_api.api.master.choice_master.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterSettingKey;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class ChoiceMasterReferApiRequest {
    ChoiceMasterSettingKey key;

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

    public static ChoiceMasterReferApiRequest from(JsonNode request) {

        return new ChoiceMasterReferApiRequest(
                getParamForKey(request)
        );
    }

    private static ChoiceMasterSettingKey getParamForKey(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "key")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return ChoiceMasterSettingKey.of(request.get("key").asText());
        }
    }
}
