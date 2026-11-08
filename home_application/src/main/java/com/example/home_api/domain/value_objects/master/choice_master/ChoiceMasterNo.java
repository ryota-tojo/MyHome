package com.example.home_api.domain.value_objects.master.choice_master;

import lombok.Value;

@Value
public class ChoiceMasterNo {
    int value;

    public static ChoiceMasterNo of(int value){
        return new ChoiceMasterNo(value);
    }
}
