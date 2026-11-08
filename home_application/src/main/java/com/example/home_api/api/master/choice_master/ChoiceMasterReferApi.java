package com.example.home_api.api.master.choice_master;

import com.example.home_api.api.common.response.ApiResponse;
import com.example.home_api.api.common.response.ApiResponseErrorData;
import com.example.home_api.api.master.choice_master.request.ChoiceMasterReferApiRequest;
import com.example.home_api.api.master.choice_master.response.ChoiceMasterReferResponseData;
import com.example.home_api.api.master.choice_master.response.ChoiceMasterResponse;
import com.example.home_api.api.master.master_setting.request.MasterSettingReferApiRequest;
import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.ChoiceMasterEntity;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.common.ParamItem;
import com.example.home_api.domain.common.ParamType;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterSettingKey;
import com.example.home_api.service.master.choice_master.ChoiceMasterReferService;
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
@RequestMapping("/api/choice_master")
public class ChoiceMasterReferApi {

    private final ChoiceMasterReferService service;

    public ChoiceMasterReferApi(ChoiceMasterReferService service) {
        this.service = service;
    }

    @PostMapping("/refer")
    public ResponseEntity<ApiResponse<?>> refer(
            @RequestBody JsonNode request
    ) {
        // パラメータチェック
        ParamCheckResult paramcheckResult = ChoiceMasterReferApiRequest.paramCheck(request);
        if(!paramcheckResult.isResult()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.createFailureBodyForParamCheckError(
                            ResponseMessageItem.失敗_参照,
                            ItemNames.選択肢マスタ,
                            paramcheckResult.getMessages())
            );
        }

        // パラメータ取得
        ChoiceMasterReferApiRequest params = ChoiceMasterReferApiRequest.from(request);

        // サービス層処理
        ReferResult<ChoiceMasterEntity> result = service.invoke(params.getKey());

        // サービス層処理結果エラーハンドリング
        if (result.getHttpStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createFailureBody(result));
        }
        return ResponseEntity.ok(createSuccessBody(result));
    }

    public ApiResponse<ChoiceMasterReferResponseData> createSuccessBody(ReferResult<ChoiceMasterEntity> result) {
        List<ChoiceMasterResponse> responses =
                result.getEntities().stream().map(
                        ChoiceMasterResponse::of
                ).toList();

        ChoiceMasterReferResponseData data =
                ChoiceMasterReferResponseData.of(responses);

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }

    public ApiResponse<ApiResponseErrorData> createFailureBody(ReferResult<ChoiceMasterEntity> result) {

        ApiResponseErrorData data =
                ApiResponseErrorData.from(result.getErrorMessage());

        return new ApiResponse<>(
                result.getResponseStatus().toString(),
                result.getResponseMessage().getValue(),
                data
        );
    }
}

