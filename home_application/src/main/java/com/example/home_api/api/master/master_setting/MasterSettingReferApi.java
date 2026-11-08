package com.example.home_api.api.master.master_setting;

import com.example.home_api.api.common.response.ApiResponse;
import com.example.home_api.api.common.response.ApiResponseErrorData;
import com.example.home_api.api.master.master_setting.request.MasterSettingReferApiRequest;
import com.example.home_api.api.master.master_setting.response.MasterSettingReferResponseData;
import com.example.home_api.api.master.master_setting.response.MasterSettingResponse;
import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.common.ParamItem;
import com.example.home_api.domain.common.ParamType;
import com.example.home_api.service.master.master_setting.MasterSettingReferService;
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
@RequestMapping("/api/master-setting")
public class MasterSettingReferApi {

    private final MasterSettingReferService service;

    public MasterSettingReferApi(MasterSettingReferService service) {
        this.service = service;
    }

    @PostMapping("/refer")
    public ResponseEntity<ApiResponse<?>> refer(
            @RequestBody JsonNode request
    ) {
        // パラメータチェック
        ParamCheckResult paramcheckResult = MasterSettingReferApiRequest.paramCheck(request);
        if(!paramcheckResult.isResult()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.createFailureBodyForParamCheckError(
                            ResponseMessageItem.失敗_参照,
                            ItemNames.マスタ設定,
                            paramcheckResult.getMessages())
            );
        }

        // パラメータ取得
        MasterSettingReferApiRequest params = MasterSettingReferApiRequest.from(request);

        // サービス層処理
        ReferResult<MasterSettingEntity> result = service.invoke(params.getKey(), params.getOffset(), params.getLimit());

        // サービス層処理結果エラーハンドリング
        if (result.getHttpStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createFailureBody(result));
        }
        return ResponseEntity.ok(createSuccessBody(result));
    }

    public ApiResponse<MasterSettingReferResponseData> createSuccessBody(ReferResult<MasterSettingEntity> result) {
        List<MasterSettingResponse> responses =
                result.getEntities().stream().map(
                        MasterSettingResponse::of
                ).toList();

        MasterSettingReferResponseData data =
                MasterSettingReferResponseData.of(responses);

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }

    public ApiResponse<ApiResponseErrorData> createFailureBody(ReferResult<MasterSettingEntity> result) {

        ApiResponseErrorData data =
                ApiResponseErrorData.from(result.getErrorMessage());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }
}

