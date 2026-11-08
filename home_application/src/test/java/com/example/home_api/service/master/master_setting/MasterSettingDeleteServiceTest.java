package com.example.home_api.service.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingDeleteRepository;
import com.example.home_api.domain.result.DeleteResult;
import com.example.home_api.domain.result.FixtureDeleteResult;
import com.example.home_api.domain.result.FixtureReferResult;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.FixtureSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MasterSettingDeleteServiceTest {

    private MasterSettingDeleteRepository repository;
    private MasterSettingReferService referService;
    private MasterSettingDeleteService service;

    @BeforeEach
    void setUp() {
        repository = mock(MasterSettingDeleteRepository.class);
        referService = mock(MasterSettingReferService.class);
        service = new MasterSettingDeleteService(repository, referService);
    }

    @ParameterizedTest
    @MethodSource("cases")
    public void masterSettingDeleteService(String useCase, MasterSettingKey key, ReferResult<MasterSettingEntity> referResult, int count, DeleteResult expected) {

        when(referService.invoke(key, null, null))
                .thenReturn(referResult);

        when(repository.delete(key)).thenReturn(1);
        DeleteResult result = service.invoke(key);

        assertEquals(expected, result);

        verify(repository, times(count)).delete(key);
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of("正常系 - 通常", FixtureSettingKey.通常(), FixtureReferResult.マスタ設定_成功_通常(), 1, FixtureDeleteResult.成功_通常(ItemNames.マスタ設定)),
                Arguments.of("異常系 - キー：未登録", FixtureSettingKey.通常(), FixtureReferResult.マスタ設定_成功_空のリスト(), 0, FixtureDeleteResult.失敗_404_主キー未登録(ItemNames.マスタ設定, "key", "setting_key"))
        );
    }


    @ParameterizedTest
    @MethodSource("exceptionErrorCases")
    public void masterSettingDeleteServiceExceptionError(String useCase, MasterSettingKey key, ReferResult<MasterSettingEntity> referResult, int count, DeleteResult expected) {

        when(referService.invoke(key, null, null))
                .thenReturn(referResult);

        when(repository.delete(key))
                .thenThrow(new RuntimeException("DB error"));
        DeleteResult result = service.invoke(key);

        assertEquals(expected, result);

        verify(repository, times(count)).delete(key);
    }

    static Stream<Arguments> exceptionErrorCases() {
        return Stream.of(
                Arguments.of("異常系 - キー：想定外", FixtureSettingKey.通常(), FixtureReferResult.マスタ設定_成功_通常(), 1, FixtureDeleteResult.失敗_500(ItemNames.マスタ設定))
        );
    }

}