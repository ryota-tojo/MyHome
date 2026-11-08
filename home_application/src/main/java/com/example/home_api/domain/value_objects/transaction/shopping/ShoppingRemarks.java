package com.example.home_api.domain.value_objects.transaction.shopping;

import lombok.Value;

@Value
public class ShoppingRemarks {
    String value;

    public static ShoppingRemarks of(String value) {
        return new ShoppingRemarks(value);
    }
}
