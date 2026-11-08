package com.example.home_api.domain.value_objects.transaction.comment;

import lombok.Value;

@Value
public class CommentTitle {
    String value;

    public static CommentTitle of(String value) {
        return new CommentTitle(value);
    }
}
