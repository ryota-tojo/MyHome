package com.example.home_api.service.master.master_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingReferRepository;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import org.springframework.stereotype.Service;

@Service
public class MasterSettingReferService {

    private final MasterSettingReferRepository repository;

    public MasterSettingReferService(MasterSettingReferRepository repository) {
        this.repository = repository;
    }

    public ReferResult<MasterSettingEntity> invoke(MasterSettingKey key, Offset offset, Limit limit) {
        try{
            return ReferResult.fromSuccess(
                    ItemNames.マスタ設定,
                    repository.find(key,offset,limit)
            );

        }catch (Exception e) {
            System.out.println(e.getMessage());
            return ReferResult.fromInternalServerError(ItemNames.マスタ設定);
        }
    }
}
