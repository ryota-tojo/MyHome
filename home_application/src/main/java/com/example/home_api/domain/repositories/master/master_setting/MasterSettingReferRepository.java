package com.example.home_api.domain.repositories.master.master_setting;

import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;

import java.util.List;

public interface MasterSettingReferRepository {

    List<MasterSettingEntity> find(MasterSettingKey key, Offset offset, Limit limit);

}
