package com.example.home_api.domain.value_objects;

import java.time.LocalDateTime;

public class FixtureUpdateDatetime {

    public static UpdateDatetime 通常() {
        return UpdateDatetime.of(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }

}