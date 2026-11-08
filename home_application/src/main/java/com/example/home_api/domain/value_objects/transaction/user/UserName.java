package com.example.home_api.domain.value_objects.transaction.user;

import lombok.Value;

@Value
public class UserName {
    String value;

    public static UserName of(String value) {
        return new UserName(value);
    }
}
