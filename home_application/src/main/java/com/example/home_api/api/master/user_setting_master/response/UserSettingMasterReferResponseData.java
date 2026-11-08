package com.example.home_api.api.master.user_setting_master.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class UserSettingMasterReferResponseData {

    @JsonProperty("settings")
    List<UserSettingMasterResponse> groupSettingMasters;

    public static UserSettingMasterReferResponseData of(List<UserSettingMasterResponse> responses) {
        return new UserSettingMasterReferResponseData(responses);
    }
}
