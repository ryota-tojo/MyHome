package com.example.home_api.api.master.group_setting_master.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class GroupSettingMasterReferResponseData {

    @JsonProperty("settings")
    List<GroupSettingMasterResponse> groupSettingMasters;

    public static GroupSettingMasterReferResponseData of(List<GroupSettingMasterResponse> responses) {
        return new GroupSettingMasterReferResponseData(responses);
    }
}
