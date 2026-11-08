package com.example.home_api.api.common.response;

import com.example.home_api.domain.result.objects.ErrorMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ApiResponseErrorData {

    @JsonProperty("message")
    String message;

    public static ApiResponseErrorData from(ErrorMessage message) {
        return new ApiResponseErrorData(
                message.getValue()
        );
    }
}
