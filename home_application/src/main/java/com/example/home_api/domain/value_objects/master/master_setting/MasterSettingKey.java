package com.example.home_api.domain.value_objects.master.master_setting;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class MasterSettingKey {
    String value;

    private static final Logger log = LoggerFactory.getLogger(MasterSettingKey.class);

    private MasterSettingKey(String value) {
        this.value = value;
    }

    public static MasterSettingKey of(String value) {
        return new MasterSettingKey(value);
    }
}
