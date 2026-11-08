package com.example.home_api.domain.value_objects.transaction.user_setting;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class UserSettingValue {
    String value;

    private static final Logger log = LoggerFactory.getLogger(UserSettingValue.class);

    private UserSettingValue(String value) {
        this.value = value;
    }

    public static UserSettingValue of(String value) {
        return new UserSettingValue(value);
    }
}
