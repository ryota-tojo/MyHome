package com.example.home_api.domain.value_objects.transaction.group_setting;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class GroupSettingValue {
    String value;

    private static final Logger log = LoggerFactory.getLogger(GroupSettingValue.class);

    private GroupSettingValue(String value) {
        this.value = value;
    }

    public static GroupSettingValue of(String value) {
        return new GroupSettingValue(value);
    }
}
