package com.example.home_api.domain.value_objects;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ApproveDatetime {
    private LocalDateTime value;

    public static ApproveDatetime of(
            LocalDateTime value) {
        return new ApproveDatetime(value);
    }
}
