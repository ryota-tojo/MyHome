package com.example.home_api.domain.common;

import lombok.Value;

@Value
public class ParamItem {
    String key;
    Object object;
    ParamType paramType;

    public static ParamItem of(String key, Object object, ParamType paramType) {
        return new ParamItem(key, object, paramType);
    }
}
