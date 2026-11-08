package com.example.home_api.domain.value_objects;

import java.time.LocalDateTime;

public class FixtureCreateDatetime {

    public static CreateDatetime 通常() {
        return CreateDatetime.of(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }

}