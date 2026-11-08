package com.example.home_api.domain.value_objects.transaction.communication;

import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.result.objects.ErrorMessage;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class ReturnFlag {
    int value;

    public static final String PARAM_NAME = "return_flag";

    public static ReturnFlag of(int value) {
        return new ReturnFlag(value);
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
        int value = node.asInt();
        if (value < 0 || 1 < value) {
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_数値範囲エラー.getValue(), PARAM_NAME, "0", "1"))
            );
        }
        return ParamCheckResult.of(
                true,
                ErrorMessage.from("")
        );
    }
}
