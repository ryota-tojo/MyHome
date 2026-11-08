package com.example.home_api.domain.value_objects.transaction.shopping;

import lombok.Value;

@Value
public class ShoppingId {
    int value;

    public static ShoppingId of(int value) {
        return new ShoppingId(value);
    }
}
