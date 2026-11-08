package com.example.home_api.service.transaction.group;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.entities.transaction.group.GroupAndSettingEntity;
import com.example.home_api.domain.entities.transaction.group.GroupEntity;
import com.example.home_api.domain.entities.transaction.group.GroupSettingEntity;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.result.ReferResult;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group.GroupName;
import com.example.home_api.domain.value_objects.transaction.group.GroupPassword;
import com.example.home_api.service.transaction.group_setting.GroupSettingReferService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupReferService {

    GroupReferRepository repository;
    GroupSettingReferService settingReferService;

    public GroupReferService(GroupReferRepository repository, GroupSettingReferService settingReferService) {
        this.repository = repository;
        this.settingReferService = settingReferService;
    }

    public ReferResult<GroupAndSettingEntity> invoke(GroupId groupId, GroupPassword groupPassword, GroupName name, Offset offset, Limit limit) {
        try {

            List<GroupEntity> userEntity = repository.find(groupId, groupPassword, name, offset, limit);
            List<GroupAndSettingEntity> entities = userEntity.stream().map(entity -> {
                List<GroupSettingEntity> settingEntities = settingReferService.invoke(entity.getGroupId(), null).getEntities();
                return GroupAndSettingEntity.of(entity, settingEntities);
            }).toList();

            return ReferResult.fromSuccess(ItemNames.所属グループ, entities);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ReferResult.fromInternalServerError(ItemNames.所属グループ);
        }
    }
}
