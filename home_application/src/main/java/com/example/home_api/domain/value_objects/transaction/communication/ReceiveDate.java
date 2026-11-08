package com.example.home_api.domain.value_objects.transaction.communication;

import lombok.Value;

import java.time.LocalDate;

@Value
public class ReceiveDate {
    private LocalDate value;

    public static ReceiveDate of(
            LocalDate value) {
        return new ReceiveDate(value);
    }
}
