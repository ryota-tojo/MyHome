package com.example.home_api.api.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.FixtureMasterSettingEntity;
import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.result.objects.ErrorMessage;
import com.example.home_api.domain.result.objects.ErrorMessageItem;
import com.example.home_api.domain.result.objects.ResponseMessageItem;
import com.example.home_api.domain.result.objects.ResponseStatus;
import com.example.home_api.service.master.master_setting.MasterSettingReferService;
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
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MasterSettingReferApi.class)
@Import(MasterSettingReferApiTest.TestConfig.class)
class MasterSettingReferApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MasterSettingReferService service;

    static class TestConfig {
        @Bean
        MasterSettingReferService masterSettingReferService() {
            return Mockito.mock(MasterSettingReferService.class);
        }
    }

    @ParameterizedTest
    @MethodSource("successCases")
    void referApiSuccess(String useCase, ReferResult<MasterSettingEntity> result) throws Exception {

        when(service.invoke(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(result);

        String requestJson = """
                {
                    "key": "someKey",
                    "offset": 0,
                    "limit": 10
                }
                """;

        ResultActions actions = mockMvc.perform(post("/api/master-setting/refer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ResponseStatus.success.toString()))
                .andExpect(jsonPath("$.message").value(String.format(ResponseMessageItem.成功_参照.getValue(), ItemNames.マスタ設定.getItemName())))
                .andExpect(jsonPath("$.data.settings").isArray());

        if (!result.getEntities().isEmpty()) {
            actions
                    .andExpect(jsonPath("$.data.settings[0].key").value(FixtureMasterSettingEntity.通常().getSettingKey().getValue()))
                    .andExpect(jsonPath("$.data.settings[0].value").value(FixtureMasterSettingEntity.通常().getSettingValue().getValue()))
                    .andExpect(jsonPath("$.data.settings[0].remarks").value(FixtureMasterSettingEntity.通常().getSettingRemarks().getValue()));
        }
    }

    static Stream<Arguments> successCases() {
        return Stream.of(
                Arguments.of("正常系 - 結果あり", ReferResult.fromSuccess(ItemNames.マスタ設定, FixtureMasterSettingEntity.リスト_通常())),
                Arguments.of("正常系 - 結果なし", ReferResult.fromSuccess(ItemNames.マスタ設定, FixtureMasterSettingEntity.リスト_空()))
        );
    }

    @ParameterizedTest
    @MethodSource("errorCases")
    void referApiError(String useCase, ReferResult<MasterSettingEntity> result) throws Exception {

        when(service.invoke(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(result);

        String requestJson = """
                {
                    "key": "someKey",
                    "offset": 0,
                    "limit": 10
                }
                """;

        mockMvc.perform(post("/api/master-setting/refer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value(ResponseStatus.error.toString()))
                .andExpect(jsonPath("$.message").value(String.format(ResponseMessageItem.失敗_参照.getValue(), ItemNames.マスタ設定.getItemName())))
                .andExpect(jsonPath("$.data.message").value(ErrorMessage.from(ErrorMessageItem.想定外エラー.getValue()).getValue()));
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                Arguments.of("異常系 - 想定外エラー", ReferResult.fromInternalServerError(ItemNames.マスタ設定))
        );
    }

}