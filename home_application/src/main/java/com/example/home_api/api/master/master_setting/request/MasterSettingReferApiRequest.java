package com.example.home_api.api.master.master_setting.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class MasterSettingReferApiRequest {
    MasterSettingKey key;
    Offset offset;
    Limit limit;

    public static ParamCheckResult paramCheck(JsonNode request) {

        ParamCheckResult paramCheckResultForKey = ParamUtils.isValidCheckForOptional(request, "key");
        if (!paramCheckResultForKey.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForKey.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForOffset = ParamUtils.isValidCheckForOptional(request, "offset");
        if (!paramCheckResultForOffset.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForOffset.getMessages()
            );
        }
        ParamCheckResult validateForOffset = Offset.validate(request);
        if (!validateForOffset.isResult()) {
            return ParamCheckResult.of(
                    false,
                    validateForOffset.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForLimit = ParamUtils.isValidCheckForOptional(request, "limit");
        if (!paramCheckResultForLimit.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForLimit.getMessages()
            );
        }
        ParamCheckResult validateForLimit = Limit.validate(request);
        if (!validateForLimit.isResult()) {
            return ParamCheckResult.of(
                    false,
                    validateForLimit.getMessages()
            );
        }
        return ParamCheckResult.of(
                true,
                null
        );
    }

    public static MasterSettingReferApiRequest from(JsonNode request) {

        return new MasterSettingReferApiRequest(
                getParamForKey(request),
                getParamForOffset(request),
                getParamForLimit(request)
        );
    }

    private static MasterSettingKey getParamForKey(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "key")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return MasterSettingKey.of(request.get("key").asText());
        }
    }

    private static Offset getParamForOffset(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "offset")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return Offset.of(request.get("offset").asInt());
        }
    }

    private static Limit getParamForLimit(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "limit")) {
            case キーなし:
            case 値なし:
                return null;
            default:
                return Limit.of(request.get("limit").asInt());
        }
    }
}
