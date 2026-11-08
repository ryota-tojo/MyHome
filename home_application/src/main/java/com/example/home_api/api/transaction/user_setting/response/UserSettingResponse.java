package com.example.home_api.api.transaction.user_setting.response;

import com.example.home_api.domain.entities.transaction.user.UserSettingEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class UserSettingResponse {

    String key;
    String value;

    @JsonProperty("create_date")
    String createDate;

    @JsonProperty("update_date")
    String updateDate;

    public static UserSettingResponse of(UserSettingEntity entity) {
        return new UserSettingResponse(
                entity.getKey().getValue(),
                entity.getValue().getValue(),
                entity.getCreateDatetime().getValue().toString(),
                entity.getUpdateDatetime().getValue().toString()
        );
    }
}
