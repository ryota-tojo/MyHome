package com.example.home_api.domain.value_objects;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class DeleteDatetime {
    private LocalDateTime value;

    public static DeleteDatetime of(
            LocalDateTime value) {
        return new DeleteDatetime(value);
    }
}
