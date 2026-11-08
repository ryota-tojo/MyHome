package com.example.home_api.domain.result.objects;


import lombok.Value;

@Value
public class ResponseMessage {
    String value;

    public static ResponseMessage from(String value){
        return new ResponseMessage(value);
    }
}
