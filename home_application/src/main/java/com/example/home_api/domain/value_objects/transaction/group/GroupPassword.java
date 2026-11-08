package com.example.home_api.domain.value_objects.transaction.group;

import lombok.Value;

@Value
public class GroupPassword {
    String value;

    public static GroupPassword of(String value) {
        return new GroupPassword(value);
    }
}
