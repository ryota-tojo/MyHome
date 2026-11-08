package com.example.home_api.domain.repositories.master.master_setting;

import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;

public interface MasterSettingDeleteRepository {

    Integer delete(MasterSettingKey key);

}
