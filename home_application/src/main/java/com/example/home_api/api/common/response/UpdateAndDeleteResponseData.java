package com.example.home_api.api.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class UpdateAndDeleteResponseData {

    @JsonProperty("affected_rows")
    Integer affectedRows;

    public static UpdateAndDeleteResponseData from(Integer affectedRows) {
        return new UpdateAndDeleteResponseData(
                affectedRows
        );
    }
}
