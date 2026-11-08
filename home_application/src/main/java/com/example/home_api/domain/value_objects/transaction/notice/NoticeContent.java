package com.example.home_api.domain.value_objects.transaction.notice;

import lombok.Value;

@Value
public class NoticeContent {
    String value;

    public static NoticeContent of(String value) {
        return new NoticeContent(value);
    }
}
