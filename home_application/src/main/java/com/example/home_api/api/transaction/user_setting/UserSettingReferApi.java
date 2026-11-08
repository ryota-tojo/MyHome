package com.example.home_api.api.transaction.user_setting;

import com.example.home_api.api.common.response.ApiResponse;
import com.example.home_api.api.common.response.ApiResponseErrorData;
import com.example.home_api.api.transaction.user_setting.request.UserSettingReferApiRequest;
import com.example.home_api.api.transaction.user_setting.response.UserSettingReferResponseData;
import com.example.home_api.api.transaction.user_setting.response.UserSettingResponse;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.entities.transaction.user.UserSettingEntity;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.service.transaction.user_setting.UserSettingReferService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user-setting")
public class UserSettingReferApi {

    private final UserSettingReferService service;

    public UserSettingReferApi(UserSettingReferService service) {
        this.service = service;
    }

    @PostMapping("/refer")
    public ResponseEntity<ApiResponse<?>> refer(
            @RequestBody JsonNode request
    ) {
        // パラメータチェック
        ParamCheckResult paramcheckResult = UserSettingReferApiRequest.paramCheck(request);
        if(!paramcheckResult.isResult()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.createFailureBodyForParamCheckError(
                            ResponseMessageItem.失敗_参照,
                            ItemNames.ユーザー設定,
                            paramcheckResult.getMessages())
            );
        }

        // パラメータ取得
        UserSettingReferApiRequest params = UserSettingReferApiRequest.from(request);

        // サービス層処理
        ReferResult<UserSettingEntity> result = service.invoke(params.getUserId(), params.getKey());

        // サービス層処理結果エラーハンドリング
        if (result.getHttpStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createFailureBody(result));
        }
        return ResponseEntity.ok(createSuccessBody(result));
    }

    public ApiResponse<UserSettingReferResponseData> createSuccessBody(ReferResult<UserSettingEntity> result) {
        List<UserSettingResponse> responses =
                result.getEntities().stream().map(
                        UserSettingResponse::of
                ).toList();

        UserSettingReferResponseData data =
                UserSettingReferResponseData.of(responses);

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }

    public ApiResponse<ApiResponseErrorData> createFailureBody(ReferResult<UserSettingEntity> result) {

        ApiResponseErrorData data =
                ApiResponseErrorData.from(result.getErrorMessage());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }
}

