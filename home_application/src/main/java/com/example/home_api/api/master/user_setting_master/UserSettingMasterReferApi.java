package com.example.home_api.api.master.user_setting_master;

import com.example.home_api.api.common.response.ApiResponse;
import com.example.home_api.api.common.response.ApiResponseErrorData;
import com.example.home_api.api.master.group_setting_master.request.GroupSettingMasterReferApiRequest;
import com.example.home_api.api.master.user_setting_master.request.UserSettingMasterReferApiRequest;
import com.example.home_api.api.master.user_setting_master.response.UserSettingMasterReferResponseData;
import com.example.home_api.api.master.user_setting_master.response.UserSettingMasterResponse;
import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.common.ParamItem;
import com.example.home_api.domain.common.ParamType;
import com.example.home_api.domain.entities.master.UserSettingMasterEntity;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.example.home_api.service.master.user_setting_master.UserSettingMasterReferService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user-setting-master")
public class UserSettingMasterReferApi {

    private final UserSettingMasterReferService service;

    public UserSettingMasterReferApi(UserSettingMasterReferService service) {
        this.service = service;
    }

    @PostMapping("/refer")
    public ResponseEntity<ApiResponse<?>> refer(
            @RequestBody JsonNode request
    ) {
        // パラメータチェック
        ParamCheckResult paramcheckResult = UserSettingMasterReferApiRequest.paramCheck(request);
        if(!paramcheckResult.isResult()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.createFailureBodyForParamCheckError(
                            ResponseMessageItem.失敗_参照,
                            ItemNames.ユーザー設定マスタ,
                            paramcheckResult.getMessages())
            );
        }

        // パラメータ取得
        UserSettingMasterReferApiRequest params = UserSettingMasterReferApiRequest.from(request);

        // サービス層処理
        ReferResult<UserSettingMasterEntity> result = service.invoke(params.getKey());

        // サービス層処理結果エラーハンドリング
        if (result.getHttpStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createFailureBody(result));
        }
        return ResponseEntity.ok(createSuccessBody(result));
    }

    public ApiResponse<UserSettingMasterReferResponseData> createSuccessBody(ReferResult<UserSettingMasterEntity> result) {
        List<UserSettingMasterResponse> responses =
                result.getEntities().stream().map(
                        UserSettingMasterResponse::of
                ).toList();

        UserSettingMasterReferResponseData data =
                UserSettingMasterReferResponseData.of(responses);

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }

    public ApiResponse<ApiResponseErrorData> createFailureBody(ReferResult<UserSettingMasterEntity> result) {

        ApiResponseErrorData data =
                ApiResponseErrorData.from(result.getErrorMessage());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }
}

