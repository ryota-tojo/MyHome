package com.example.home_api.api.master.master_setting.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class MasterSettingUpdateApiRequest {
    MasterSettingKey key;
    MasterSettingValue value;
    MasterSettingRemarks remarks;

    public static ParamCheckResult paramCheck(JsonNode request) {

        ParamCheckResult paramCheckResultForKey = ParamUtils.isValidCheckForRequired(request, "key");
        if(!paramCheckResultForKey.isResult()){
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForKey.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForValue = ParamUtils.isValidCheckForOptional(request, "value");
        if(!paramCheckResultForValue.isResult()){
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForValue.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForRemarks = ParamUtils.isValidCheckForOptional(request, "remarks");
        if(!paramCheckResultForRemarks.isResult()){
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

    public static MasterSettingUpdateApiRequest from(JsonNode request) {

        return new MasterSettingUpdateApiRequest(
                getParamForKey(request),
                getParamForValue(request),
                getParamForRemarks(request)
        );
    }

    private static MasterSettingKey getParamForKey(JsonNode request) {
        switch (ParamUtils.getParamCondition(request,"key")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<key>");
                throw new IllegalArgumentException();
            default:
                return MasterSettingKey.of(request.get("key").asText());
        }
    }
    private static MasterSettingValue getParamForValue(JsonNode request) {
        switch (ParamUtils.getParamCondition(request,"value")) {
            case キーなし:
                return null;
            default:
                return MasterSettingValue.of(request.get("value").asText());
        }
    }
    private static MasterSettingRemarks getParamForRemarks(JsonNode request) {
        switch (ParamUtils.getParamCondition(request,"remarks")) {
            case キーなし:
                return null;
            default:
                return MasterSettingRemarks.of(request.get("remarks").asText());
        }
    }
}
