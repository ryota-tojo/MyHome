package com.example.home_api.domain.value_objects.transaction.group;

import lombok.Value;

@Value
public class GroupId {
    String value;

    public static GroupId of(String value) {
        return new GroupId(value);
    }
}
