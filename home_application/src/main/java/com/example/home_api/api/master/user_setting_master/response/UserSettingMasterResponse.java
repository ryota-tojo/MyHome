package com.example.home_api.api.master.user_setting_master.response;

import com.example.home_api.domain.entities.master.UserSettingMasterEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class UserSettingMasterResponse {

    String key;
    String init_value;
    String remarks;

    @JsonProperty("create_date")
    String createDate;

    @JsonProperty("update_date")
    String updateDate;

    public static UserSettingMasterResponse of(UserSettingMasterEntity entity) {
        return new UserSettingMasterResponse(
                entity.getKey().getValue(),
                entity.getInitValue().getValue(),
                entity.getRemarks().getValue(),
                entity.getCreateDatetime().getValue().toString(),
                entity.getUpdateDatetime().getValue().toString()
        );
    }
}
