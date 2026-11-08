package com.example.home_api.service.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingCreateRepository;
import com.example.home_api.domain.result.*;
import com.example.home_api.domain.value_objects.*;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MasterSettingCreateServiceTest {

    private MasterSettingCreateRepository repository;
    private MasterSettingReferService referService;
    private MasterSettingCreateService service;

    @BeforeEach
    void setUp() {
        repository = mock(MasterSettingCreateRepository.class);
        referService = mock(MasterSettingReferService.class);
        service = new MasterSettingCreateService(repository, referService);
    }

    @ParameterizedTest
    @MethodSource("cases")
    public void masterSettingCreateService(String useCase, MasterSettingKey key, MasterSettingValue value, MasterSettingRemarks remarks, ReferResult<MasterSettingEntity> referResult, int count, CreateResult<String> expected) {

        when(referService.invoke(key, null, null))
                .thenReturn(referResult);

        when(repository.create(key, value, remarks)).thenReturn("key");
        CreateResult<String> result = service.invoke(key, value, remarks);

        assertEquals(expected, result);

        verify(repository, times(count)).create(key, value, remarks);
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of("正常系 - 通常", FixtureSettingKey.通常(), FixtureSettingValue.通常(), FixtureSettingRemarks.通常(), FixtureReferResult.マスタ設定_成功_空のリスト(), 1, FixtureCreateResult.成功_通常(ItemNames.マスタ設定, "key")),
                Arguments.of("異常系 - キー：重複", FixtureSettingKey.通常(), FixtureSettingValue.通常(), FixtureSettingRemarks.通常(), FixtureReferResult.マスタ設定_成功_通常(), 0, FixtureCreateResult.失敗_409_主キー重複(ItemNames.マスタ設定, FixtureSettingKey.通常().getValue(),FixtureSettingValue.通常().getValue()))
        );
    }


    @ParameterizedTest
    @MethodSource("exceptionErrorCases")
    public void masterSettingCreateServiceExceptionError(String useCase, MasterSettingKey key, MasterSettingValue value, MasterSettingRemarks remarks, ReferResult<MasterSettingEntity> referResult, int count, CreateResult<String> expected) {
        when(referService.invoke(key, null, null))
                .thenReturn(referResult);

        CreateResult<String> result = service.invoke(key, value, remarks);

        assertEquals(expected, result);

        verify(repository, times(count)).create(key, value, remarks);
    }

    static Stream<Arguments> exceptionErrorCases() {
        return Stream.of(
                Arguments.of("異常系 - キー：想定外", FixtureSettingKey.通常(), FixtureSettingValue.通常(), FixtureSettingRemarks.通常(), FixtureReferResult.マスタ設定_失敗_想定外エラー(), 0, FixtureCreateResult.失敗_500(ItemNames.マスタ設定))
        );
    }

}