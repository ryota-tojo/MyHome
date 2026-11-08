package com.example.home_api.domain.value_objects.transaction.communication;

import lombok.Value;

@Value
public class ReturnRemarks {
    String value;

    public static ReturnRemarks of(String value) {
        return new ReturnRemarks(value);
    }
}
