package com.example.home_api.domain.value_objects.transaction.notice;

import lombok.Value;

@Value
public class NoticeId {
    int value;

    public static NoticeId of(int value) {
        return new NoticeId(value);
    }
}
