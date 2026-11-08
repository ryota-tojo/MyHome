package com.example.home_api.api.master.master_setting.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class MasterSettingReferResponseData {

    @JsonProperty("settings")
    List<MasterSettingResponse> masterSettings;

    public static MasterSettingReferResponseData of(List<MasterSettingResponse> responses) {
        return new MasterSettingReferResponseData(responses);
    }
}
