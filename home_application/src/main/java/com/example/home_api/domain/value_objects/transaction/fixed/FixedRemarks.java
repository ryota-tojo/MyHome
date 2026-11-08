package com.example.home_api.domain.value_objects.transaction.fixed;

import lombok.Value;

@Value
public class FixedRemarks {
    String value;

    public static FixedRemarks of(String value) {
        return new FixedRemarks(value);
    }
}
