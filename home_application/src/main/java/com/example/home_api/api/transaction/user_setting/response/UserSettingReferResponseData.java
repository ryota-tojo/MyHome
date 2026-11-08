package com.example.home_api.api.transaction.user_setting.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class UserSettingReferResponseData {

    @JsonProperty("settings")
    List<UserSettingResponse> settings;

    public static UserSettingReferResponseData of(List<UserSettingResponse> responses) {
        return new UserSettingReferResponseData(responses);
    }
}
