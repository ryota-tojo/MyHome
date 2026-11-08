package com.example.home_api.api.master.choice_master.response;

import com.example.home_api.domain.entities.master.ChoiceMasterEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ChoiceMasterResponse {

    int id;
    String key;
    int no;
    String value;

    @JsonProperty("create_date")
    String createDate;

    @JsonProperty("update_date")
    String updateDate;

    public static ChoiceMasterResponse of(ChoiceMasterEntity entity) {
        return new ChoiceMasterResponse(
                entity.getId().getValue(),
                entity.getKey().getValue(),
                entity.getNo().getValue(),
                entity.getValue().getValue(),
                entity.getCreateDatetime().getValue().toString(),
                entity.getUpdateDatetime().getValue().toString()
        );
    }
}
