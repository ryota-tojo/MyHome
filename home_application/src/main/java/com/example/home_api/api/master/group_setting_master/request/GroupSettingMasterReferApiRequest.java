package com.example.home_api.api.master.group_setting_master.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class GroupSettingMasterReferApiRequest {
    GroupSettingMasterKey key;

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

    public static GroupSettingMasterReferApiRequest from(JsonNode request) {

        return new GroupSettingMasterReferApiRequest(
                getParamForKey(request)
        );
    }

    private static GroupSettingMasterKey getParamForKey(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "key")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return GroupSettingMasterKey.of(request.get("key").asText());
        }
    }
}
