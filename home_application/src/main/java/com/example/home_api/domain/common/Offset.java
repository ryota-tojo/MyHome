package com.example.home_api.domain.common;

import com.example.home_api.domain.result.objects.ErrorMessage;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class Offset {
    int value;

    public static final String PARAM_NAME = "offset";

    public static Offset of (int value) {
        return new Offset(value);
    }

    public static ParamCheckResult validate(JsonNode params) {
        // 前提のパラメータチェックで確認済みのため、キーなし、値なしは許容する
        if (!params.has(PARAM_NAME)) {
            return ParamCheckResult.of(
                    true,
                    ErrorMessage.from("")
            );
        }
        JsonNode node = params.get(PARAM_NAME);
        if (node == null || node.isNull()) {
            return ParamCheckResult.of(
                    true,
                    ErrorMessage.from("")
            );
        }

        if (!node.isInt()) {
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_データ型エラー_数値以外.getValue(), PARAM_NAME))
            );
        }
        return ParamCheckResult.of(
                true,
                ErrorMessage.from("")
        );
    }
}
