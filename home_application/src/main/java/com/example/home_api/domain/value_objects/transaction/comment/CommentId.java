
package com.example.home_api.domain.value_objects.transaction.comment;

import lombok.Value;

@Value
public class CommentId {
    String value;

    public static CommentId of(String value) {
        return new CommentId(value);
    }
}
