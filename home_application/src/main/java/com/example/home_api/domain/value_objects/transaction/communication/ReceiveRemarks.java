package com.example.home_api.domain.value_objects.transaction.communication;

import lombok.Value;

@Value
public class ReceiveRemarks {
    String value;

    public static ReceiveRemarks of(String value) {
        return new ReceiveRemarks(value);
    }
}
