package com.example.home_api.api.master.group_setting_master;

import com.example.home_api.api.common.response.ApiResponse;
import com.example.home_api.api.common.response.ApiResponseErrorData;
import com.example.home_api.api.common.response.UpdateAndDeleteResponseData;
import com.example.home_api.api.master.group_setting_master.request.GroupSettingMasterDeleteApiRequest;
import com.example.home_api.api.master.master_setting.request.MasterSettingDeleteApiRequest;
import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.common.ParamItem;
import com.example.home_api.domain.common.ParamType;
import com.example.home_api.domain.result.DeleteResult;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.example.home_api.service.master.group_setting_master.GroupSettingMasterDeleteService;
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
public class GroupSettingMasterDeleteApi {

    private final GroupSettingMasterDeleteService service;

    public GroupSettingMasterDeleteApi(GroupSettingMasterDeleteService service) {
        this.service = service;
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<?>> delete(
            @RequestBody JsonNode request
    ) {
        // パラメータチェック
        ParamCheckResult paramcheckResult = GroupSettingMasterDeleteApiRequest.paramCheck(request);
        if(!paramcheckResult.isResult()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.createFailureBodyForParamCheckError(
                            ResponseMessageItem.失敗_削除,
                            ItemNames.所属グループ設定マスタ,
                            paramcheckResult.getMessages())
            );
        }

        // パラメータ取得
        GroupSettingMasterDeleteApiRequest params = GroupSettingMasterDeleteApiRequest.from(request);

        // サービス層処理
        DeleteResult result = service.invoke(params.getKey());

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

    public ApiResponse<UpdateAndDeleteResponseData> createSuccessBody(DeleteResult result) {

        UpdateAndDeleteResponseData data =
                UpdateAndDeleteResponseData.from(result.getAffectedRows());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }

    public ApiResponse<ApiResponseErrorData> createFailureBody(DeleteResult result) {

        ApiResponseErrorData data =
                ApiResponseErrorData.from(result.getErrorMessage());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }
}

