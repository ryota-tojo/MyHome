package com.example.home_api.domain.value_objects.transaction.shopping;

import lombok.Value;

import java.time.LocalDate;

@Value
public class ShoppingDate {
    private LocalDate value;

    public static ShoppingDate of(
            LocalDate value) {
        return new ShoppingDate(value);
    }
}
