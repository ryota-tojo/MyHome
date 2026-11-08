package com.example.home_api.service.master.master_setting;

import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.entities.FixtureMasterSettingEntity;
import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingReferRepository;
import com.example.home_api.domain.result.FixtureReferResult;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.FixtureSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MasterSettingReferServiceTest {

    private MasterSettingReferRepository repository;
    private MasterSettingReferService service;

    @BeforeEach
    void setUp() {
        repository = mock(MasterSettingReferRepository.class);
        service = new MasterSettingReferService(repository);
    }

    @ParameterizedTest
    @MethodSource("successCases")
    public void masterSettingReferServiceSuccess(String useCase, MasterSettingKey key, Offset offset, Limit limit, List<MasterSettingEntity> entity, ReferResult<MasterSettingEntity> expected) {

        when(repository.find(key, offset, limit)).thenReturn(entity);
        ReferResult<MasterSettingEntity> result = service.invoke(key, offset, limit);

        assertEquals(expected, result);

        verify(repository, times(1)).find(key, offset, limit);
    }

    static Stream<Arguments> successCases() {
        return Stream.of(
                Arguments.of("正常系 - 通常", FixtureSettingKey.通常(), Offset.of(0), Limit.of(10), FixtureMasterSettingEntity.リスト_通常(), FixtureReferResult.マスタ設定_成功_通常()),
                Arguments.of("正常系 - 返却値：空のリスト", FixtureSettingKey.通常(), Offset.of(0), Limit.of(10), FixtureMasterSettingEntity.リスト_空(), FixtureReferResult.マスタ設定_成功_空のリスト())
        );
    }


    @ParameterizedTest
    @MethodSource("exceptionErrorCases")
    public void masterSettingReferServiceExceptionError(String useCase, MasterSettingKey key, Offset offset, Limit limit) {

        when(repository.find(key, offset, limit)).thenThrow(new RuntimeException("DB error"));
        ReferResult<MasterSettingEntity> result = service.invoke(key, offset, limit);

        assertEquals(FixtureReferResult.マスタ設定_失敗_想定外エラー(), result);

        verify(repository, times(1)).find(key, offset, limit);
    }

    static Stream<Arguments> exceptionErrorCases() {
        return Stream.of(
                Arguments.of("異常系 - 想定外エラー", FixtureSettingKey.通常(), Offset.of(0), Limit.of(10))
        );
    }

}