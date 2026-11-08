package com.example.home_api.api.master.master_setting.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class MasterSettingDeleteApiRequest {
    MasterSettingKey key;

    public static ParamCheckResult paramCheck(JsonNode request) {

        ParamCheckResult paramCheckResultForKey = ParamUtils.isValidCheckForOptional(request, "key");
        if(!paramCheckResultForKey.isResult()){
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

    public static MasterSettingDeleteApiRequest from(JsonNode request) {

        return new MasterSettingDeleteApiRequest(
                getParamForKey(request)
        );
    }

    private static MasterSettingKey getParamForKey(JsonNode request) {
        switch (ParamUtils.getParamCondition(request,"key")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return MasterSettingKey.of(request.get("key").asText());
        }
    }
}
