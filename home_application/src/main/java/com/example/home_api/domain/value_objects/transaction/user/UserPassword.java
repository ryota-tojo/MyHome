package com.example.home_api.domain.value_objects.transaction.user;

import lombok.Value;

@Value
public class UserPassword {
    String value;

    public static UserPassword of(String value) {
        return new UserPassword(value);
    }
}
