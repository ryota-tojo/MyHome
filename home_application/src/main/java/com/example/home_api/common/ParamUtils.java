package com.example.home_api.common;

import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.common.ParamCondition;
import com.example.home_api.domain.common.ParamItem;
import com.example.home_api.domain.common.ParamType;
import com.example.home_api.domain.result.objects.ErrorMessage;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/*
  以下については本クラスでハンドリングする
  ・パラメータそのものが存在しない場合
  ・パラメータの値がNullの場合
  ・パラメータの値が空欄の場合
  ・パラメータの値が0以下の場合
  これ以外のバリデーションについては別で対応する
 */
public class ParamUtils {

    private static final Logger log = LoggerFactory.getLogger(ParamUtils.class);

    public static ParamCheckResult isParamCheck(JsonNode params, List<ParamItem> paramItems) {

        log.debug("call isParamCheck");
        List<ErrorMessage> errorMessageList = paramItems.stream()
                .map(paramItem -> {
                    String key = paramItem.getKey();
                    ParamType type = paramItem.getParamType();
                    ErrorMessage errorMessage = ErrorMessage.from(String.format(ErrorMessageItem.パラメータチェックエラー.getValue(), type, key));

                    log.debug("  -> paramItem: {}", paramItem);

                    switch (type) {
                        case 主キー:
                            log.debug("  -> isValidPrimaryParam");
                            if (!isValidPrimaryParam(params, key)) {
                                return errorMessage;
                            }
                            return null;
                        case 主キー_参照系:
                            log.debug("  -> isValidPrimaryParamForRefer");
                            if (!isValidPrimaryParamForRefer(params, key)) {
                                return errorMessage;
                            }
                            return null;
                        case 必須キー:
                            log.debug("  -> isValidRequiredParam");
                            if (!isValidRequiredParam(params, key)) {
                                return errorMessage;
                            }
                            return null;
                        case 必須キー_参照系:
                            log.debug("  -> isValidRequiredParamForRefer");
                            if (!isValidRequiredParamForRefer(params, key)) {
                                return errorMessage;
                            }
                            return null;
                        case 任意キー:
                            log.debug("  -> isValidOptionalParam");
                            if (!isValidOptionalParam(params, key)) {
                                return errorMessage;
                            }
                            return null;
                        default:
                            log.error("想定外の ParamType: key={}, type={}", key, type);
                            throw new IllegalArgumentException("想定外の ParamType です");
                    }

                })
                .filter(Objects::nonNull)
                .toList();

        return ParamCheckResult.of(
                errorMessageList.isEmpty(),
                null
        );
    }

    // 主キー（参照系以外のAPI用）
    private static boolean isValidPrimaryParam(JsonNode params, String key) {

        // キーなしNG
        if (!params.has(key)) {
            log.warn("パラメータチェックエラー：主キーが存在しない ({})", key);
            return false;
        }
        JsonNode node = params.get(key);

        // null NG
        if (node.isNull()) {
            log.warn("パラメータチェックエラー：主キーがNull ({})", key);
            return false;
        }

        // 文字列型の場合
        if (node.isTextual()) {
            String strValue = node.asText();

            // 空欄 NG
            if (strValue.isEmpty()) {
                log.warn("パラメータチェックエラー：主キーが空欄 ({})", key);
                return false;
            }
            // 使用不可文字 NG
            if (ValidateUtils.isContainsInvalidChars(strValue)) {
                log.warn("パラメータチェックエラー：主キーが使用不可文字を含む ({})", key);
                return false; // 使用不可文字
            }

        }
        // 数値の場合
        else if (node.isNumber()) {
            // 0以下 NG
            if (node.asInt() <= 0) {
                log.warn("パラメータチェックエラー：主キーが0以下 ({})", key);
                return false;
            }
        }
        // 想定外のデータ型 NG
        else {
            log.warn("パラメータチェックエラー：主キー想定外のデータ型 ({})", key);
            return false;
        }
        return true;
    }

    // 主キー（参照系のAPI用）
    private static boolean isValidPrimaryParamForRefer(JsonNode params, String key) {
        if (!params.has(key)) {
            return true; // キーなしOK
        }
        JsonNode node = params.get(key);

        if (node.isNull()) {
            return true; // null OK
        }
        // 文字列型の場合
        if (node.isTextual()) {
            String strValue = node.asText();

            // 空欄 NG
            if (strValue.isEmpty()) {
                log.warn("パラメータチェックエラー：主キーが空欄 ({})", key);
                return false;
            }
            // 使用不可文字 NG
            if (ValidateUtils.isContainsInvalidChars(strValue)) {
                log.warn("パラメータチェックエラー：主キーが使用不可文字を含む ({})", key);
                return false; // 使用不可文字
            }

        }
        // 数値の場合
        else if (node.isNumber()) {
            // 0以下 NG
            if (node.asInt() <= 0) {
                log.warn("パラメータチェックエラー：主キーが0以下 ({})", key);
                return false;
            }
        }
        // 想定外のデータ型 NG
        else {
            log.warn("パラメータチェックエラー：主キー想定外のデータ型 ({})", key);
            return false;
        }
        return true;
    }

    // 必須キー（参照系以外のAPI用）
    private static boolean isValidRequiredParam(JsonNode params, String key) {
        if (!params.has(key)) {
            log.warn("パラメータチェックエラー：必須キーが存在しない ({})", key);
            return false; // キーなしNG
        }
        JsonNode node = params.get(key);

        if (node.isNull()) {
            log.warn("パラメータチェックエラー：必須キーがNull ({})", key);
            return false; // null NG
        }
        // 文字列型の場合
        if (node.isTextual()) {
            String strValue = node.asText();

            // 空欄 NG
            if (strValue.isEmpty()) {
                log.warn("パラメータチェックエラー：必須キーが空欄 ({})", key);
                return false;
            }
            // 使用不可文字 NG
            if (ValidateUtils.isContainsInvalidChars(strValue)) {
                log.warn("パラメータチェックエラー：必須キーが使用不可文字を含む ({})", key);
                return false; // 使用不可文字
            }
        }
        return true;
    }

    // 必須キー（参照系のAPI用）
    private static boolean isValidRequiredParamForRefer(JsonNode params, String key) {
        if (!params.has(key)) {
            log.warn("パラメータチェックエラー：必須キーが存在しない ({})", key);
            return false; // キーなしNG
        }
        JsonNode node = params.get(key);

        if (node.isNull()) {
            return true; // null OK
        }
        // 文字列型の場合
        if (node.isTextual()) {
            String strValue = node.asText();

            // 使用不可文字 NG
            if (ValidateUtils.isContainsInvalidChars(strValue)) {
                log.warn("パラメータチェックエラー：必須キーが使用不可文字を含む ({})", key);
                return false; // 使用不可文字
            }
        }
        return true;
    }

    // 任意キー
    private static boolean isValidOptionalParam(JsonNode params, String key) {
        JsonNode node = params.get(key);
        if (node.isNull()) {
            return true; // null OK
        }
        if (ValidateUtils.isContainsInvalidChars(node.asText())) {
            log.warn("パラメータチェックエラー：任意キーが使用不可文字を含む ({})", key);
            return false; // 使用不可文字
        }
        return true;
    }


    /*
    【パラメータチェック】キー：必須 & 値：必須
     */
    public static ParamCheckResult isValidCheckForRequired(JsonNode params, String paramName) {
        // キーなしNG
        if (!params.has(paramName)) {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーが存在しない.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーが存在しない.getValue(), paramName))
            );
        }
        JsonNode node = params.get(paramName);

        // null NG
        if (node.isNull()) {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーの値が存在しない.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーの値が存在しない.getValue(), paramName))
            );
        }

        // 文字列型の場合
        if (node.isTextual()) {
            String strValue = node.asText();

            // 空欄 NG
            if (strValue.isEmpty()) {
                log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_値が空欄.getValue(), paramName)).getValue());
                return ParamCheckResult.of(
                        false,
                        ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_値が空欄.getValue(), paramName))
                );
            }
            // 使用不可文字 NG
            if (ValidateUtils.isContainsInvalidChars(strValue)) {
                log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName)).getValue());
                return ParamCheckResult.of(
                        false,
                        ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName))
                );
            }

        }
        // 数値の場合
        else if (node.isNumber()) {


        }
        // 想定外のデータ型 NG
        else {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName))
            );
        }
        return ParamCheckResult.of(
                true,
                ErrorMessage.from("")
        );
    }

    /*
   【パラメータチェック】 キー：必須 & 値：空欄許容
     */
    public static ParamCheckResult isValidCheckForRequiredAllowEmpty(JsonNode params, String paramName) {
        // キーなしNG
        if (!params.has(paramName)) {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーが存在しない.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーが存在しない.getValue(), paramName))
            );
        }
        JsonNode node = params.get(paramName);

        // null NG
        if (node.isNull()) {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーの値が存在しない.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーの値が存在しない.getValue(), paramName))
            );
        }

        // 文字列型の場合
        if (node.isTextual()) {
            String strValue = node.asText();

            // 使用不可文字 NG
            if (ValidateUtils.isContainsInvalidChars(strValue)) {
                log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName)).getValue());
                return ParamCheckResult.of(
                        false,
                        ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName))
                );
            }

        }
        // 数値の場合
        else if (node.isNumber()) {


        }
        // 想定外のデータ型 NG
        else {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName))
            );
        }
        return ParamCheckResult.of(
                true,
                ErrorMessage.from("")
        );
    }

    /*
   【パラメータチェック】 キー：必須 & 値：null許容
     */
    public static ParamCheckResult isValidCheckForRequiredAllowNull(JsonNode params, String paramName) {
        // キーなしNG
        if (!params.has(paramName)) {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーが存在しない.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーが存在しない.getValue(), paramName))
            );
        }
        JsonNode node = params.get(paramName);

        // 文字列型の場合
        if (node.isTextual()) {
            String strValue = node.asText();

            // 空欄 NG
            if (strValue.isEmpty()) {
                log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_値が空欄.getValue(), paramName)).getValue());
                return ParamCheckResult.of(
                        false,
                        ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_値が空欄.getValue(), paramName))
                );
            }
            // 使用不可文字 NG
            if (ValidateUtils.isContainsInvalidChars(strValue)) {
                log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName)).getValue());
                return ParamCheckResult.of(
                        false,
                        ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName))
                );
            }

        }
        // 数値の場合
        else if (node.isNumber()) {


        }
        // 想定外のデータ型 NG
        else {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName))
            );
        }
        return ParamCheckResult.of(
                true,
                ErrorMessage.from("")
        );
    }

    /*
   【パラメータチェック】 キー：必須 & 値：空欄 & null許容
     */
    public static ParamCheckResult isValidCheckForRequiredAllowEmptyAndNull(JsonNode params, String paramName) {
        // キーなしNG
        if (!params.has(paramName)) {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーが存在しない.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_必須キーが存在しない.getValue(), paramName))
            );
        }
        JsonNode node = params.get(paramName);

        // 文字列型の場合
        if (node.isTextual()) {
            String strValue = node.asText();

            // 使用不可文字 NG
            if (ValidateUtils.isContainsInvalidChars(strValue)) {
                log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName)).getValue());
                return ParamCheckResult.of(
                        false,
                        ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName))
                );
            }

        }
        // 数値の場合
        else if (node.isNumber()) {


        }
        // 想定外のデータ型 NG
        else {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName))
            );
        }
        return ParamCheckResult.of(
                true,
                ErrorMessage.from("")
        );
    }

    /*
   【パラメータチェック】 キー：任意 & 値：空欄 & null許容
     */
    public static ParamCheckResult isValidCheckForOptional(JsonNode params, String paramName) {
        // キーなしOK
        if (!params.has(paramName)) {
            return ParamCheckResult.of(
                    true,
                    ErrorMessage.from("")
            );
        }
        JsonNode node = params.get(paramName);
        if (node == null || node.isNull()) {
            return ParamCheckResult.of(
                    true,
                    ErrorMessage.from("")
            );
        }
        // 文字列型の場合
        if (node.isTextual()) {
            String strValue = node.asText();

            // 使用不可文字 NG
            if (ValidateUtils.isContainsInvalidChars(strValue)) {
                log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName)).getValue());
                return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_使用不可文字を含む.getValue(), paramName))
            );
            }

        }
        // 数値の場合
        else if (node.isNumber()) {


        }
        // 想定外のデータ型 NG
        else {
            log.warn(ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName)).getValue());
            return ParamCheckResult.of(
                    false,
                    ErrorMessage.from(String.format(ErrorMessageItem.バリデーションエラー_想定外のデータ型.getValue(), paramName))
            );
        }
        return ParamCheckResult.of(
                true,
                ErrorMessage.from("")
        );
    }


    //
    /*
   【パラメータ状態取得】 ※パラメータチェック後に使用すること
     */
    public static ParamCondition getParamCondition(JsonNode params, String paramName) {
        if (!params.has(paramName)) {
            return ParamCondition.キーなし;
        }

        JsonNode node = params.get(paramName);
        if (node.isNull()) {
            return ParamCondition.値なし;
        }
        return ParamCondition.値あり;
    }


}
