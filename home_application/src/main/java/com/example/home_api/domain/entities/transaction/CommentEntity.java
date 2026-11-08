package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.comment.CommentContent;
import com.example.home_api.domain.value_objects.transaction.comment.CommentId;
import com.example.home_api.domain.value_objects.transaction.comment.CommentTitle;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import lombok.Value;

@Value
public class CommentEntity {
    CommentId id;
    GroupId groupId;
    CommentTitle title;
    CommentContent content;
    UserId CreateUserId;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static CommentEntity of(
            CommentId id,
            GroupId groupId,
            CommentTitle title,
            CommentContent content,
            UserId CreateUserId,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new CommentEntity(
                id,
                groupId,
                title,
                content,
                CreateUserId,
                createDatetime,
                updateDatetime
        );
    }
}
