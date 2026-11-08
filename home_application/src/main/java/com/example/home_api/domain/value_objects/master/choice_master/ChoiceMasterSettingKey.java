package com.example.home_api.domain.value_objects.master.choice_master;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class ChoiceMasterSettingKey {
    String value;

    private static final Logger log = LoggerFactory.getLogger(ChoiceMasterSettingKey.class);

    private ChoiceMasterSettingKey(String value) {
        this.value = value;
    }

    public static ChoiceMasterSettingKey of(String value) {
        return new ChoiceMasterSettingKey(value);
    }
}
