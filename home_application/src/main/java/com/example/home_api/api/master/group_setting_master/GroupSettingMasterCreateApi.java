package com.example.home_api.api.master.group_setting_master;

import com.example.home_api.api.common.response.ApiResponse;
import com.example.home_api.api.common.response.ApiResponseErrorData;
import com.example.home_api.api.common.response.CreateResponseDataForString;
import com.example.home_api.api.master.group_setting_master.request.GroupSettingMasterCreateApiRequest;
import com.example.home_api.api.master.master_setting.request.MasterSettingCreateApiRequest;
import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.common.ParamItem;
import com.example.home_api.domain.common.ParamType;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterRemarks;
import com.example.home_api.service.master.group_setting_master.GroupSettingMasterCreateService;
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
@RequestMapping("/api/group-setting-master")
public class GroupSettingMasterCreateApi {

    private final GroupSettingMasterCreateService service;

    public GroupSettingMasterCreateApi(GroupSettingMasterCreateService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> create(
            @RequestBody JsonNode request
    ) {
        // パラメータチェック
        ParamCheckResult paramcheckResult = GroupSettingMasterCreateApiRequest.paramCheck(request);
        if(!paramcheckResult.isResult()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.createFailureBodyForParamCheckError(
                            ResponseMessageItem.失敗_作成,
                            ItemNames.所属グループ設定マスタ,
                            paramcheckResult.getMessages())
            );
        }

        // パラメータ取得
        GroupSettingMasterCreateApiRequest params = GroupSettingMasterCreateApiRequest.from(request);

        // サービス層処理
        CreateResult<String> result = service.invoke(params.getKey(), params.getValue(), params.getRemarks());

        // サービス層処理結果エラーハンドリング
        if (result.getHttpStatus().equals(HttpStatus.BAD_REQUEST)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureBody(result));
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

