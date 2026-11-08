package com.example.home_api.domain.result.objects;

import lombok.Value;

@Value
public class ValidationResult {

    ValidationCheckStatus status;
    String keyword;
    String value;
    ErrorMessageItem errorMessageItem;

    public static ValidationResult from(ValidationCheckStatus status,String keyword,String value,ErrorMessageItem errorMessageItem){
        return new ValidationResult(status,keyword,value,errorMessageItem);
    }
}
