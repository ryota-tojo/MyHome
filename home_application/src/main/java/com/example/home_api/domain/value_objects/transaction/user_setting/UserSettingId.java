package com.example.home_api.domain.value_objects.transaction.user_setting;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class UserSettingId {
    String value;

    private static final Logger log = LoggerFactory.getLogger(UserSettingId.class);

    private UserSettingId(String value) {
        this.value = value;
    }

    public static UserSettingId of(String value) {
        return new UserSettingId(value);
    }
}
