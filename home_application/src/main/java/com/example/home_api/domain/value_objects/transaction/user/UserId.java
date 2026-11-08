package com.example.home_api.domain.value_objects.transaction.user;

import lombok.Value;

@Value
public class UserId {
    String value;

    public UserId(String value) {
        this.value = value;
    }

    public static UserId of(String value) {
        return new UserId(value);
    }
}
