package com.example.home_api.domain.value_objects.transaction.communication;

import lombok.Value;

@Value
public class ReceiveTo {
    String value;

    public static ReceiveTo of(String value) {
        return new ReceiveTo(value);
    }
}
