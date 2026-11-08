package com.example.home_api.api.transaction.user;

import com.example.home_api.api.common.response.ApiResponse;
import com.example.home_api.api.common.response.ApiResponseErrorData;
import com.example.home_api.api.transaction.user.request.UserReferApiRequest;
import com.example.home_api.api.transaction.user.response.UserReferResponseData;
import com.example.home_api.api.transaction.user.response.UserResponse;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.entities.transaction.user.UserAndSettingEntity;
import com.example.home_api.domain.entities.transaction.user.UserEntity;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.service.transaction.user.UserReferService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserReferApi {

    private final UserReferService service;

    public UserReferApi(UserReferService service) {
        this.service = service;
    }

    @PostMapping("/refer")
    public ResponseEntity<ApiResponse<?>> refer(
            @RequestBody JsonNode request
    ) {
        // パラメータチェック
        ParamCheckResult paramcheckResult = UserReferApiRequest.paramCheck(request);
        if(!paramcheckResult.isResult()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.createFailureBodyForParamCheckError(
                            ResponseMessageItem.失敗_参照,
                            ItemNames.ユーザー,
                            paramcheckResult.getMessages())
            );
        }

        // パラメータ取得
        UserReferApiRequest params = UserReferApiRequest.from(request);

        // サービス層処理
        ReferResult<UserAndSettingEntity> result = service.invoke(
                params.getUserId(),
                params.getPassword(),
                params.getName(),
                params.getPermissionflag(),
                params.getApproveflag(),
                params.getDeleteflag(),
                params.getOffset(),
                params.getLimit()
        );

        // サービス層処理結果エラーハンドリング
        if (result.getHttpStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createFailureBody(result));
        }
        return ResponseEntity.ok(createSuccessBody(result));
    }

    public ApiResponse<UserReferResponseData> createSuccessBody(ReferResult<UserAndSettingEntity> result) {
        List<UserResponse> responses =
                result.getEntities().stream().map(
                        UserResponse::of
                ).toList();

        UserReferResponseData data =
                UserReferResponseData.of(responses);

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }

    public ApiResponse<ApiResponseErrorData> createFailureBody(ReferResult<UserAndSettingEntity> result) {

        ApiResponseErrorData data =
                ApiResponseErrorData.from(result.getErrorMessage());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }
}

