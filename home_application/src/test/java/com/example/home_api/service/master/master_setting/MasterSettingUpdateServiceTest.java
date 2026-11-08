package com.example.home_api.service.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingUpdateRepository;
import com.example.home_api.domain.result.FixtureReferResult;
import com.example.home_api.domain.result.FixtureUpdateResult;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.result.UpdateResult;
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

class MasterSettingUpdateServiceTest {

    private MasterSettingUpdateRepository repository;
    private MasterSettingReferService referService;
    private MasterSettingUpdateService service;

    @BeforeEach
    void setUp() {
        repository = mock(MasterSettingUpdateRepository.class);
        referService = mock(MasterSettingReferService.class);
        service = new MasterSettingUpdateService(repository, referService);
    }

    @ParameterizedTest
    @MethodSource("cases")
    public void masterSettingUpdateService(String useCase, MasterSettingKey key, MasterSettingValue value, MasterSettingRemarks remarks, ReferResult<MasterSettingEntity> referResult, int count, UpdateResult expected) {

        when(referService.invoke(key, null, null))
                .thenReturn(referResult);

        when(repository.update(key, value, remarks)).thenReturn(1);
        UpdateResult result = service.invoke(key, value, remarks);

        assertEquals(expected, result);

        verify(repository, times(count)).update(key, value, remarks);
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of("正常系 - 通常", FixtureSettingKey.通常(), FixtureSettingValue.通常(), FixtureSettingRemarks.通常(), FixtureReferResult.マスタ設定_成功_通常(), 1, FixtureUpdateResult.成功_通常(ItemNames.マスタ設定)),
                Arguments.of("異常系 - キー：未登録", FixtureSettingKey.通常(), FixtureSettingValue.通常(), FixtureSettingRemarks.通常(), FixtureReferResult.マスタ設定_成功_空のリスト(), 0, FixtureUpdateResult.失敗_404_主キー未登録(ItemNames.マスタ設定, "key", "setting_key"))
        );
    }


    @ParameterizedTest
    @MethodSource("exceptionErrorCases")
    public void masterSettingUpdateServiceExceptionError(String useCase, MasterSettingKey key, MasterSettingValue value, MasterSettingRemarks remarks, ReferResult<MasterSettingEntity> referResult, int count, UpdateResult expected) {

        when(referService.invoke(key, null, null))
                .thenReturn(referResult);

        when(repository.update(key, value, remarks)).thenThrow(new RuntimeException("DB error"));
        UpdateResult result = service.invoke(key, value, remarks);

        assertEquals(expected, result);

        verify(repository, times(count)).update(key, value, remarks);
    }

    static Stream<Arguments> exceptionErrorCases() {
        return Stream.of(
                Arguments.of("異常系 - キー：想定外", FixtureSettingKey.通常(), FixtureSettingValue.通常(), FixtureSettingRemarks.通常(), FixtureReferResult.マスタ設定_成功_通常(), 1, FixtureUpdateResult.失敗_500(ItemNames.マスタ設定))
        );
    }

}