package com.example.home_api.domain.value_objects.transaction.communication;

import lombok.Value;

import java.time.LocalDate;

@Value
public class ReceiveFrom {
    String value;

    public static ReceiveFrom of(String value) {
        return new ReceiveFrom(value);
    }
}
