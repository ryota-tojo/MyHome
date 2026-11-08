package com.example.home_api.domain.repositories.master.master_setting;

import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;

public interface MasterSettingCreateRepository {

    String create(MasterSettingKey key, MasterSettingValue value, MasterSettingRemarks remarks);

}
