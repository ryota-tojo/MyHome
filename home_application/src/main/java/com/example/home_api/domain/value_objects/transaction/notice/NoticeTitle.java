package com.example.home_api.domain.value_objects.transaction.notice;

import lombok.Value;

@Value
public class NoticeTitle {
    String value;

    public static NoticeTitle of(String value) {
        return new NoticeTitle(value);
    }
}
