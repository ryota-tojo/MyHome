package com.example.home_api.service.master.choice_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.entities.master.ChoiceMasterEntity;
import com.example.home_api.domain.repositories.master.choice_master.ChoiceSettingReferRepository;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.master.choice_master.ChoiceMasterSettingKey;
import org.springframework.stereotype.Service;

@Service
public class ChoiceMasterReferService {

    private final ChoiceSettingReferRepository repository;

    public ChoiceMasterReferService(ChoiceSettingReferRepository repository) {
        this.repository = repository;
    }

    public ReferResult<ChoiceMasterEntity> invoke(ChoiceMasterSettingKey key) {
        try {
            return ReferResult.fromSuccess(
                    ItemNames.選択肢マスタ,
                    repository.find(key)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ReferResult.fromInternalServerError(ItemNames.選択肢マスタ);
        }
    }
}
