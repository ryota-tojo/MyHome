package com.example.home_api.api.common.response;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.ParamType;
import com.example.home_api.domain.result.objects.ErrorMessage;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.result.objects.ResponseStatus;
import lombok.Value;

import java.util.List;

@Value
public class ApiResponse<T> {
    String status;
    String message;
    T data;

    public static ApiResponse<ApiResponseErrorData> createFailureBodyForParamCheckError(ResponseMessageItem responseMessageItem, ItemNames itemName, ErrorMessage messages) {

        ApiResponseErrorData data =
                ApiResponseErrorData.from(messages);

        return new ApiResponse<>(
                ResponseStatus.error.toString(),
                String.format(responseMessageItem.getValue(),itemName),
                data
        );
    }

    public static ApiResponse<ApiResponseErrorData> creatBadRequestBody(ResponseMessageItem responseMessageItem, ItemNames itemName, String key) {

        ApiResponseErrorData data =
                ApiResponseErrorData.from(ErrorMessage.from(String.format(ErrorMessageItem.必須キー未指定エラー.getValue(),key )));

        return new ApiResponse<>(
                ResponseStatus.error.toString(),
                String.format(responseMessageItem.getValue(),itemName),
                data);
    }
}
