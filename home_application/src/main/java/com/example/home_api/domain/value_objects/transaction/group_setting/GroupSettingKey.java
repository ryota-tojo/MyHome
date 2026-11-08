package com.example.home_api.domain.value_objects.transaction.group_setting;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class GroupSettingKey {
    String value;

    private static final Logger log = LoggerFactory.getLogger(GroupSettingKey.class);

    private GroupSettingKey(String value) {
        this.value = value;
    }

    public static GroupSettingKey of(String value) {
        return new GroupSettingKey(value);
    }
}
