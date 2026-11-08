package com.example.home_api.api.transaction.user_setting;

import com.example.home_api.api.common.response.ApiResponse;
import com.example.home_api.api.common.response.ApiResponseErrorData;
import com.example.home_api.api.common.response.CreateResponseDataForString;
import com.example.home_api.api.transaction.user_setting.request.UserSettingCreateApiRequest;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.result.objects.ResponseMessageItem;

import com.example.home_api.service.transaction.user_setting.UserSettingCreateService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-setting")
public class UserSettingCreateApi {

    private final UserSettingCreateService service;

    public UserSettingCreateApi(UserSettingCreateService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> create(
            @RequestBody JsonNode request
    ) {
        // パラメータチェック
        ParamCheckResult paramcheckResult = UserSettingCreateApiRequest.paramCheck(request);
        if (!paramcheckResult.isResult()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.createFailureBodyForParamCheckError(
                            ResponseMessageItem.失敗_作成,
                            ItemNames.ユーザー設定,
                            paramcheckResult.getMessages())
            );
        }

        // パラメータ取得
        UserSettingCreateApiRequest params = UserSettingCreateApiRequest.from(request);

        // サービス層処理
        CreateResult<String> result = service.invoke(params.getUserId(), params.getKey(), params.getValue());

        // サービス層処理結果エラーハンドリング
        if (result.getHttpStatus().equals(HttpStatus.BAD_REQUEST)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureBody(result));
        }
        if (result.getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createFailureBody(result));
        }
        if (result.getHttpStatus().equals(HttpStatus.CONFLICT)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(createFailureBody(result));
        }
        if (result.getHttpStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createFailureBody(result));
        }
        return ResponseEntity.ok(createSuccessBody(result));
    }

    public ApiResponse<CreateResponseDataForString> createSuccessBody(CreateResult<String> result) {

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                null
        );
    }

    public ApiResponse<ApiResponseErrorData> createFailureBody(CreateResult<String> result) {
        ApiResponseErrorData data =
                ApiResponseErrorData.from(result.getErrorMessage());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }
}

