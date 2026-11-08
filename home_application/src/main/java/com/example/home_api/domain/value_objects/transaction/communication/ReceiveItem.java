package com.example.home_api.domain.value_objects.transaction.communication;

import lombok.Value;

@Value
public class ReceiveItem {
    String value;

    public static ReceiveItem of(String value) {
        return new ReceiveItem(value);
    }
}
