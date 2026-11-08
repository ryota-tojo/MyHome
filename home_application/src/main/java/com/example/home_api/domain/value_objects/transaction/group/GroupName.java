package com.example.home_api.domain.value_objects.transaction.group;

import lombok.Value;

@Value
public class GroupName {
    String value;

    public static GroupName of(String value) {
        return new GroupName(value);
    }
}
