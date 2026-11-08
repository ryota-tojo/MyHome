package com.example.home_api.domain.value_objects.transaction.shopping;

import lombok.Value;

@Value
public class ShoppingLabelIds {
    String value;

    public static ShoppingLabelIds of(String value) {
        return new ShoppingLabelIds(value);
    }
}
