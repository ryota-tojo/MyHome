
package com.example.home_api.domain.value_objects.transaction.shopping_label;

import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.result.objects.ErrorMessage;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class ShoppingLabelNo {
    int value;

    public static final String PARAM_NAME = "no";

    public static ShoppingLabelNo of(int value) {
        return new ShoppingLabelNo(value);
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
