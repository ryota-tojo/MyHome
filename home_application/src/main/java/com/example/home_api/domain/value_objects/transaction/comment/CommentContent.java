package com.example.home_api.domain.value_objects.transaction.comment;

import lombok.Value;

@Value
public class CommentContent {
    String value;

    public static CommentContent of(String value) {
        return new CommentContent(value);
    }
}
