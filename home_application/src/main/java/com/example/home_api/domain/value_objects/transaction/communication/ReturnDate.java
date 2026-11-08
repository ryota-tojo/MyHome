package com.example.home_api.domain.value_objects.transaction.communication;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class ReturnDate {
    private LocalDate value;

    public static ReturnDate of(
            LocalDate value) {
        return new ReturnDate(value);
    }
}
