package com.example.home_api.domain.value_objects.transaction.communication;

import lombok.Value;

@Value
public class ReturnItem {
    String value;

    public static ReturnItem of(String value) {
        return new ReturnItem(value);
    }
}
