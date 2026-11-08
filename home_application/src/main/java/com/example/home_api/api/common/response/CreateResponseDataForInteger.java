package com.example.home_api.api.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CreateResponseDataForInteger {

    @JsonProperty("primary_key")
    Integer primaryKey;

    public static CreateResponseDataForInteger from(Integer primaryKey) {
        return new CreateResponseDataForInteger(
                primaryKey
        );
    }
}
