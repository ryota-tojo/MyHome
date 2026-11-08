package com.example.home_api.domain.value_objects.master.choice_master;

import lombok.Value;

@Value
public class ChoiceMasterId {
    int value;

    public static ChoiceMasterId of(int value){
        return new ChoiceMasterId(value);
    }
}
