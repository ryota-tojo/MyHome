package com.example.home_api.api.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.result.CreateResult;
import com.example.home_api.domain.result.objects.ErrorMessage;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.result.objects.ResponseStatus;
import com.example.home_api.service.master.master_setting.MasterSettingCreateService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MasterSettingCreateApi.class)
@Import(MasterSettingCreateApiTest.TestConfig.class)
class MasterSettingCreateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MasterSettingCreateService service;

    static class TestConfig {
        @Bean
        MasterSettingCreateService masterSettingCreateService() {
            return Mockito.mock(MasterSettingCreateService.class);
        }
    }

    @ParameterizedTest
    @MethodSource("successCases")
    void referApiSuccess(String useCase, CreateResult<String> result) throws Exception {

        when(service.invoke(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(result);

        String requestJson = """
                {
                    "key": "setting_key",
                    "value": "value",
                    "remarks": "remarks"
                }
                """;

        mockMvc.perform(post("/api/master-setting/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ResponseStatus.success.toString()))
                .andExpect(jsonPath("$.message").value(String.format(ResponseMessageItem.成功_作成.getValue(), ItemNames.マスタ設定.getItemName())))
                .andExpect(jsonPath("$.data.primary_key").value("setting_key"));
    }

    static Stream<Arguments> successCases() {
        return Stream.of(
                Arguments.of("正常系", CreateResult.fromSuccess(ItemNames.マスタ設定, "setting_key"))
        );
    }

    @ParameterizedTest
    @MethodSource("errorCases")
    void referApiError(String useCase, CreateResult<String> result, ResultMatcher httpStatus, ErrorMessageItem errorMessageItem) throws Exception {

        when(service.invoke(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(result);

        String requestJson = """
                {
                    "key": "setting_key",
                    "value": "value",
                    "remarks": "remarks"
                }
                """;

        mockMvc.perform(post("/api/master-setting/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(httpStatus)
                .andExpect(jsonPath("$.status").value(ResponseStatus.error.toString()))
                .andExpect(jsonPath("$.message").value(String.format(ResponseMessageItem.失敗_作成.getValue(), ItemNames.マスタ設定.getItemName())))
                .andExpect(jsonPath("$.data.message").value(ErrorMessage.from(String.format(errorMessageItem.getValue(), "key")).getValue()));
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                Arguments.of("異常系 - データ不正エラー", CreateResult.fromBadRequestErrorForMissingRequiredKey(ItemNames.マスタ設定, "key"), status().is4xxClientError(), ErrorMessageItem.必須キー未指定エラー),
                Arguments.of("異常系 - 重複エラー", CreateResult.fromConflictErrorForDuplicatePrimaryKey(ItemNames.マスタ設定, "key", ""), status().is4xxClientError(), ErrorMessageItem.主キー重複エラー),
                Arguments.of("異常系 - 想定外エラー", CreateResult.fromInternalServerError(ItemNames.マスタ設定), status().is5xxServerError(), ErrorMessageItem.想定外エラー)
        );
    }

}