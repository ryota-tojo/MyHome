package com.example.home_api.domain.result;

import com.example.home_api.common.OutputLogsForResult;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.result.objects.*;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
public class DeleteResult {
    HttpStatus httpStatus;
    ResponseStatus responseStatus;
    ResponseMessage responseMessage;
    ErrorMessage errorMessage;
    Integer affectedRows;

    /**
     * 【汎用 データ削除結果オブジェクト】200：削除成功
     * @param itemName 項目名（ユーザー設定、所属グループ情報など）
     * @param affectedRows 削除件数
     */
    public static DeleteResult fromSuccess(ItemNames itemName, Integer affectedRows) {
        OutputLogsForResult.outputLogs(
                HttpStatus.OK,
                ResponseStatus.success,
                ResponseMessage.from(String.format(ResponseMessageItem.成功_削除.getValue(), itemName.getItemName())),
                ErrorMessage.from(ErrorMessageItem.なし.getValue()),
                "対象行",
                String.valueOf(affectedRows)
        );
        return new DeleteResult(
                HttpStatus.OK,
                ResponseStatus.success,
                ResponseMessage.from(String.format(ResponseMessageItem.成功_削除.getValue(), itemName.getItemName())),
                ErrorMessage.from(ErrorMessageItem.なし.getValue()),
                affectedRows
        );
    }

    /**
     * 【汎用 データ削除結果オブジェクト】400: 必須キー未指定エラー
     * @param itemName 項目名（ユーザー設定、所属グループ情報など）
     * @param keyName 対象キー名
     */
    public static DeleteResult fromBadRequestErrorForMissingRequiredKey(ItemNames itemName, String keyName) {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ResponseStatus responseStatus = ResponseStatus.error;
        ResponseMessage responseMessage = ResponseMessage.from(String.format(ResponseMessageItem.失敗_削除.getValue(),itemName.getItemName()));
        ErrorMessage errorMessage = ErrorMessage.from(String.format(ErrorMessageItem.必須キー未指定エラー.getValue(),keyName));

        OutputLogsForResult.outputLogs(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                keyName,
                null
        );
        return new DeleteResult(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                0
        );
    }

    /**
     * 【汎用 データ削除結果オブジェクト】400: 必須キー空欄エラー
     * @param itemName 項目名（ユーザー設定、所属グループ情報など）
     * @param keyName 対象キー名
     */
    public static DeleteResult fromBadRequestErrorForRequiredKeyEmpty(ItemNames itemName, String keyName) {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ResponseStatus responseStatus = ResponseStatus.error;
        ResponseMessage responseMessage = ResponseMessage.from(String.format(ResponseMessageItem.失敗_削除.getValue(),itemName.getItemName()));
        ErrorMessage errorMessage = ErrorMessage.from(String.format(ErrorMessageItem.主キー空欄エラー.getValue(),keyName));

        OutputLogsForResult.outputLogs(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                keyName,
                ""
        );
        return new DeleteResult(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                0
        );
    }

    /**
     * 【汎用 データ削除結果オブジェクト】404: 主キー未登録エラー
     * @param itemName 項目名（ユーザー設定、所属グループ情報など）
     * @param keyName 主キー名
     * @param value 主キーの値
     */
    public static DeleteResult fromNotFoundErrorForRequiredKeyNotRegistered(ItemNames itemName, String keyName, String value) {

        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ResponseStatus responseStatus = ResponseStatus.error;
        ResponseMessage responseMessage = ResponseMessage.from(String.format(ResponseMessageItem.失敗_削除.getValue(),itemName.getItemName()));
        ErrorMessage errorMessage = ErrorMessage.from(String.format(ErrorMessageItem.主キー未登録エラー.getValue(),"'" + keyName + "':'" + value + "'"));

        OutputLogsForResult.outputLogs(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                keyName,
                value
        );
        return new DeleteResult(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                0
        );
    }

    /**
     * 【汎用 データ削除結果オブジェクト】404: 関連データ未登録エラー
     * @param itemName 項目名（ユーザー設定、所属グループ情報など）
     * @param keyName 主キー名
     * @param value 主キーの値
     */
    public static DeleteResult fromNotFoundErrorForRelatedDataNotRegistered(ItemNames itemName, String keyName, String value) {

        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ResponseStatus responseStatus = ResponseStatus.error;
        ResponseMessage responseMessage = ResponseMessage.from(String.format(ResponseMessageItem.失敗_削除.getValue(),itemName.getItemName()));
        ErrorMessage errorMessage = ErrorMessage.from(String.format(ErrorMessageItem.関連データ未登録エラー.getValue(),keyName,value));

        OutputLogsForResult.outputLogs(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                keyName,
                value
        );
        return new DeleteResult(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                0
        );
    }

    /**
     * 【汎用 データ削除結果オブジェクト】409: 関連データ重複エラー
     * @param itemName 項目名（ユーザー設定、所属グループ情報など）
     * @param keyName 主キー名
     * @param keyValue 主キーの値
     */
    public static DeleteResult fromConflictErrorForDuplicatePrimaryKey(ItemNames itemName, String keyName, String keyValue) {

        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ResponseStatus responseStatus = ResponseStatus.error;
        ResponseMessage responseMessage = ResponseMessage.from(String.format(ResponseMessageItem.失敗_削除.getValue(),itemName.getItemName()));
        ErrorMessage errorMessage = ErrorMessage.from(String.format(ErrorMessageItem.主キー重複エラー.getValue(),keyName));

        OutputLogsForResult.outputLogs(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                keyName,
                keyValue
        );
        return new DeleteResult(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                0
        );
    }

    /**
     * 【汎用 データ削除結果オブジェクト】500: 関連データ取得失敗エラー
     * @param itemName 項目名（ユーザー設定、所属グループ情報など）
     * @param keyName 主キー名
     * @param value 主キーの値
     */
    public static DeleteResult fromInternalServerErrorForRelatedDataRetrievalFailed(ItemNames itemName, String keyName, String value) {

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseStatus responseStatus = ResponseStatus.error;
        ResponseMessage responseMessage = ResponseMessage.from(String.format(ResponseMessageItem.失敗_削除.getValue(),itemName.getItemName()));
        ErrorMessage errorMessage = ErrorMessage.from(String.format(ErrorMessageItem.関連データ取得失敗エラー.getValue(),keyName,value));

        OutputLogsForResult.outputLogs(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                keyName,
                value
        );
        return new DeleteResult(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                0
        );
    }

    /**
     * 【汎用 データ削除結果オブジェクト】500: 想定外エラー
     * @param itemName 項目名（ユーザー設定、所属グループ情報など）
     */
    public static DeleteResult fromInternalServerError(ItemNames itemName) {

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseStatus responseStatus = ResponseStatus.error;
        ResponseMessage responseMessage = ResponseMessage.from(String.format(ResponseMessageItem.失敗_削除.getValue(),itemName.getItemName()));
        ErrorMessage errorMessage = ErrorMessage.from(ErrorMessageItem.想定外エラー.getValue());

        OutputLogsForResult.outputLogs(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                itemName.getItemName(),
                null
        );
        return new DeleteResult(
                httpStatus,
                responseStatus,
                responseMessage,
                errorMessage,
                0
        );
    }

}
