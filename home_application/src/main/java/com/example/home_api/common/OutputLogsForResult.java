package com.example.home_api.common;

import com.example.home_api.domain.result.objects.ErrorMessage;
import com.example.home_api.domain.result.objects.ResponseMessage;
import com.example.home_api.domain.result.objects.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.util.List;

public class OutputLogsForResult {

    public static void outputLogs(
            HttpStatus httpStatus,
            ResponseStatus responseStatus,
            ResponseMessage responseMessage,
            ErrorMessage errorMessage,
            String itemName,
            String itemValue
    ) {
        List<String> logs = List.of(
                "Httpステータス： " + toEmpty(httpStatus),
                "レスポンスステータス: " + toEmpty(responseStatus),
                "レスポンスメッセージ: " + toEmpty(responseMessage),
                "エラーメッセージ: " + toEmpty(errorMessage),
                itemName + ": " + toEmpty(itemValue)
        );

        System.out.println(
                String.join(", ", logs)
        );
    }

    private static String toEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
