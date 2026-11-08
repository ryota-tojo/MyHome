package com.example.home_api.domain.value_objects.master.user_setting_master;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class UserSettingMasterKey {
    String value;

    private static final Logger log = LoggerFactory.getLogger(UserSettingMasterKey.class);

    private UserSettingMasterKey(String value) {
        this.value = value;
    }

    public static UserSettingMasterKey of(String value) {
        return new UserSettingMasterKey(value);
    }
}
