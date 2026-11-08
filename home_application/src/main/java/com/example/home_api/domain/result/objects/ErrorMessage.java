package com.example.home_api.domain.result.objects;

import lombok.Value;

@Value
public class ErrorMessage {

    String value;

    public static ErrorMessage from(String value){
        return new ErrorMessage(value);
    }
}
