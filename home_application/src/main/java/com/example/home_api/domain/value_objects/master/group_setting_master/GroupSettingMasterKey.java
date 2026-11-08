package com.example.home_api.domain.value_objects.master.group_setting_master;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class GroupSettingMasterKey {
    String value;

    private static final Logger log = LoggerFactory.getLogger(GroupSettingMasterKey.class);

    private GroupSettingMasterKey(String value) {
        this.value = value;
    }

    public static GroupSettingMasterKey of(String value) {
        return new GroupSettingMasterKey(value);
    }
}
