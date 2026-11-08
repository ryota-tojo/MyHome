package com.example.home_api.domain.repositories.master.choice_master;

import com.example.home_api.domain.entities.master.ChoiceMasterEntity;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterSettingKey;

import java.util.List;

public interface ChoiceSettingReferRepository {

    List<ChoiceMasterEntity> find(ChoiceMasterSettingKey key);

}
