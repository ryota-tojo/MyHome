package com.example.home_api.api.transaction.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class UserReferResponseData {

    @JsonProperty("users")
    List<UserResponse> users;

    public static UserReferResponseData of(List<UserResponse> responses) {
        return new UserReferResponseData(responses);
    }
}
