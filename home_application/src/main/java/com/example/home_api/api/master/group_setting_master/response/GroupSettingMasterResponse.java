package com.example.home_api.api.master.group_setting_master.response;

import com.example.home_api.domain.entities.master.GroupSettingMasterEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class GroupSettingMasterResponse {

    String key;
    String init_value;
    String remarks;

    @JsonProperty("create_date")
    String createDate;

    @JsonProperty("update_date")
    String updateDate;

    public static GroupSettingMasterResponse of(GroupSettingMasterEntity entity) {
        return new GroupSettingMasterResponse(
                entity.getKey().getValue(),
                entity.getInitValue().getValue(),
                entity.getRemarks().getValue(),
                entity.getCreateDatetime().getValue().toString(),
                entity.getUpdateDatetime().getValue().toString()
        );
    }
}
