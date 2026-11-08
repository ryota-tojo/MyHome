package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.notice.NoticeContent;
import com.example.home_api.domain.value_objects.transaction.notice.NoticeId;
import com.example.home_api.domain.value_objects.transaction.notice.NoticeTitle;
import lombok.Value;

@Value
public class NoticeEntity {
    NoticeId id;
    NoticeTitle title;
    NoticeContent content;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static NoticeEntity of(NoticeId id, NoticeTitle title, NoticeContent content, CreateDatetime createDatetime, UpdateDatetime updateDatetime) {
        return new NoticeEntity(id, title, content, createDatetime, updateDatetime);
    }
}
