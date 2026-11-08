package com.example.home_api.domain.entities.master;

import com.example.home_api.domain.value_objects.*;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterNo;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterSettingKey;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterSettingValue;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterId;
import lombok.Value;

@Value
public class ChoiceMasterEntity {
    ChoiceMasterId id;
    ChoiceMasterSettingKey key;
    ChoiceMasterNo no;
    ChoiceMasterSettingValue value;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static ChoiceMasterEntity of(
            ChoiceMasterId id,
            ChoiceMasterSettingKey key,
            ChoiceMasterNo no,
            ChoiceMasterSettingValue value,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime) {

        return new ChoiceMasterEntity(id, key, no, value, createDatetime, updateDatetime);
    }
}
