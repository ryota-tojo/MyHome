package com.example.home_api.domain.value_objects;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class CreateDatetime {
    private LocalDateTime value;

    public static CreateDatetime of(
            LocalDateTime value) {
        return new CreateDatetime(value);
    }
}
