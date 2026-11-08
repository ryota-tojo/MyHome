package com.example.home_api.domain.value_objects.transaction.group_setting;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class GroupSettingId {
    String value;

    private static final Logger log = LoggerFactory.getLogger(GroupSettingId.class);

    private GroupSettingId(String value) {
        this.value = value;
    }

    public static GroupSettingId of(String value) {
        return new GroupSettingId(value);
    }
}
