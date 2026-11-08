package com.example.home_api.api.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.result.UpdateResult;
import com.example.home_api.domain.result.objects.ErrorMessage;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.result.objects.ResponseStatus;
import com.example.home_api.service.master.master_setting.MasterSettingUpdateService;
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

@WebMvcTest(MasterSettingUpdateApi.class)
@Import(MasterSettingUpdateApiTest.TestConfig.class)
class MasterSettingUpdateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MasterSettingUpdateService service;

    static class TestConfig {
        @Bean
        MasterSettingUpdateService masterSettingUpdateService() {
            return Mockito.mock(MasterSettingUpdateService.class);
        }
    }

    @ParameterizedTest
    @MethodSource("successCases")
    void referApiSuccess(String useCase, UpdateResult result) throws Exception {

        when(service.invoke(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(result);

        String requestJson = """
                {
                    "key": "setting_key",
                    "value": "value",
                    "remarks": "remarks"
                }
                """;

        mockMvc.perform(post("/api/master-setting/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ResponseStatus.success.toString()))
                .andExpect(jsonPath("$.message").value(String.format(ResponseMessageItem.成功_更新.getValue(), ItemNames.マスタ設定.getItemName())))
                .andExpect(jsonPath("$.data.affected_rows").value(1));
    }

    static Stream<Arguments> successCases() {
        return Stream.of(
                Arguments.of("正常系", UpdateResult.fromSuccess(ItemNames.マスタ設定, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("errorCases")
    void referApiError(String useCase, UpdateResult result, ResultMatcher httpStatus, ErrorMessageItem errorMessageItem) throws Exception {

        when(service.invoke(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(result);

        String requestJson = """
                {
                    "key": "setting_key",
                    "value": "value",
                    "remarks": "remarks"
                }
                """;

        mockMvc.perform(post("/api/master-setting/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(httpStatus)
                .andExpect(jsonPath("$.status").value(ResponseStatus.error.toString()))
                .andExpect(jsonPath("$.message").value(String.format(ResponseMessageItem.失敗_更新.getValue(), ItemNames.マスタ設定.getItemName())))
                .andExpect(jsonPath("$.data.message").value(ErrorMessage.from(String.format(errorMessageItem.getValue(), "key","hoge")).getValue()));
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                Arguments.of("異常系 - データ不正エラー", UpdateResult.fromBadRequestErrorForMissingRequiredKey(ItemNames.マスタ設定, "key"), status().is4xxClientError(), ErrorMessageItem.必須キー未指定エラー),
                Arguments.of("異常系 - 未登録エラー", UpdateResult.fromNotFoundErrorForRequiredKeyNotRegistered(ItemNames.マスタ設定, "key", "hoge"), status().is4xxClientError(), ErrorMessageItem.主キー未登録エラー),
                Arguments.of("異常系 - 想定外エラー", UpdateResult.fromInternalServerError(ItemNames.マスタ設定), status().is5xxServerError(), ErrorMessageItem.想定外エラー)
        );
    }

}