package com.example.home_api.domain.common;

import com.example.home_api.domain.result.objects.ErrorMessage;
import lombok.Value;

@Value
public class ParamCheckResult {
    boolean result;
    ErrorMessage messages;

    public static ParamCheckResult of(boolean result, ErrorMessage messages) {
        return new ParamCheckResult(result, messages);
    }
}
