package com.example.home_api.api.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CreateResponseDataForString {

    @JsonProperty("primary_key")
    String primaryKey;

    public static CreateResponseDataForString from(String primaryKey) {
        return new CreateResponseDataForString(
                primaryKey
        );
    }
}
