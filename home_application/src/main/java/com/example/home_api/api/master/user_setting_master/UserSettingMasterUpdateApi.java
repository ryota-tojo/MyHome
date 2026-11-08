package com.example.home_api.api.master.user_setting_master;

import com.example.home_api.api.common.response.ApiResponse;
import com.example.home_api.api.common.response.ApiResponseErrorData;
import com.example.home_api.api.common.response.UpdateAndDeleteResponseData;
import com.example.home_api.api.master.group_setting_master.request.GroupSettingMasterUpdateApiRequest;
import com.example.home_api.api.master.user_setting_master.request.UserSettingMasterUpdateApiRequest;
import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.common.ParamItem;
import com.example.home_api.domain.common.ParamType;
import com.example.home_api.domain.result.UpdateResult;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterKey;
import com.example.home_api.domain.value_objects.master.user_setting_master.UserSettingMasterRemarks;
import com.example.home_api.service.master.user_setting_master.UserSettingMasterUpdateService;
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
public class UserSettingMasterUpdateApi {

    private final UserSettingMasterUpdateService service;

    public UserSettingMasterUpdateApi(UserSettingMasterUpdateService service) {
        this.service = service;
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<?>> update(
            @RequestBody JsonNode request
    ) {
        // パラメータチェック
        ParamCheckResult paramcheckResult = UserSettingMasterUpdateApiRequest.paramCheck(request);
        if(!paramcheckResult.isResult()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.createFailureBodyForParamCheckError(
                            ResponseMessageItem.失敗_更新,
                            ItemNames.ユーザー設定マスタ,
                            paramcheckResult.getMessages())
            );
        }

        // パラメータ取得
        UserSettingMasterUpdateApiRequest params = UserSettingMasterUpdateApiRequest.from(request);

        // サービス層処理
        UpdateResult result = service.invoke(params.getKey(), params.getValue(), params.getRemarks());

        // サービス層処理結果エラーハンドリング
        if (result.getHttpStatus().equals(HttpStatus.BAD_REQUEST)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureBody(result));
        }
        if (result.getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createFailureBody(result));
        }
        if (result.getHttpStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createFailureBody(result));
        }
        return ResponseEntity.ok(createSuccessBody(result));
    }

    public ApiResponse<UpdateAndDeleteResponseData> createSuccessBody(UpdateResult result) {

        UpdateAndDeleteResponseData data =
                UpdateAndDeleteResponseData.from(result.getAffectedRows());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }

    public ApiResponse<ApiResponseErrorData> createFailureBody(UpdateResult result) {

        ApiResponseErrorData data =
                ApiResponseErrorData.from(result.getErrorMessage());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }
}

