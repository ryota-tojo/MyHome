package com.example.home_api.domain.value_objects.transaction.user_setting;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class UserSettingKey {
    String value;

    private static final Logger log = LoggerFactory.getLogger(UserSettingKey.class);

    private UserSettingKey(String value) {
        this.value = value;
    }

    public static UserSettingKey of(String value) {
        return new UserSettingKey(value);
    }
}
