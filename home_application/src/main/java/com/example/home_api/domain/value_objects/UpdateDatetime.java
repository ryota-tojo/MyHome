package com.example.home_api.domain.value_objects;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class UpdateDatetime {
    private LocalDateTime value;

    public static UpdateDatetime of(
            LocalDateTime value) {
        return new UpdateDatetime(value);
    }
}
