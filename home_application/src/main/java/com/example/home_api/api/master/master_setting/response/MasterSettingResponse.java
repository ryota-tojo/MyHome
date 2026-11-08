package com.example.home_api.api.master.master_setting.response;

import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class MasterSettingResponse {

    String key;
    String value;
    String remarks;

    @JsonProperty("create_date")
    String createDate;

    @JsonProperty("update_date")
    String updateDate;

    public static MasterSettingResponse of(MasterSettingEntity entity) {
        return new MasterSettingResponse(
                entity.getSettingKey().getValue(),
                entity.getSettingValue().getValue(),
                entity.getSettingRemarks().getValue(),
                entity.getCreateDatetime().getValue().toString(),
                entity.getUpdateDatetime().getValue().toString()
        );
    }
}
