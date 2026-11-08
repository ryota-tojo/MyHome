package com.example.home_api.domain.value_objects.transaction.group_member;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class GroupMemberId {
    String value;

    private static final Logger log = LoggerFactory.getLogger(GroupMemberId.class);

    private GroupMemberId(String value) {
        this.value = value;
    }

    public static GroupMemberId of(String value) {
        return new GroupMemberId(value);
    }
}
