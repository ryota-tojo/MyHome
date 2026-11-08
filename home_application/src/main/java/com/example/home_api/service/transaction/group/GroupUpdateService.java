package com.example.home_api.service.transaction.group;

import com.example.home_api.domain.common.ItemNames;

import com.example.home_api.domain.entities.transaction.group.GroupEntity;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.repositories.transaction.group.GroupUpdateRepository;
import com.example.home_api.domain.result.UpdateResult;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group.GroupName;
import com.example.home_api.domain.value_objects.transaction.group.GroupPassword;
import com.example.home_api.domain.value_objects.transaction.user.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupUpdateService {

    GroupReferRepository referRepository;
    GroupUpdateRepository updateRepository;

    public GroupUpdateService(GroupReferRepository referRepository,
                              GroupUpdateRepository updateRepository) {
        this.referRepository = referRepository;
        this.updateRepository = updateRepository;
    }

    public UpdateResult invoke(GroupId groupId, GroupPassword password, GroupName name) {
        // 所属グループ未登録チェック
        List<GroupEntity> entities =  referRepository.find(groupId,null,null,null,null);
        if(entities.size() == 0){
            return UpdateResult.fromNotFoundErrorForRelatedDataNotRegistered(
                    ItemNames.所属グループ,
                    "group_id",
                    groupId.getValue()
            );
        }
        try {
            return UpdateResult.fromSuccess(
                    ItemNames.所属グループ,
                    updateRepository.update(groupId, password, name)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return UpdateResult.fromInternalServerError(ItemNames.所属グループ);
        }
    }
}
