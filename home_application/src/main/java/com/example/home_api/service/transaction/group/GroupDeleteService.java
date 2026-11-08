package com.example.home_api.service.transaction.group;

import com.example.home_api.domain.common.ItemNames;

import com.example.home_api.domain.entities.transaction.group.GroupEntity;
import com.example.home_api.domain.repositories.transaction.group.GroupDeleteRepository;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.result.DeleteResult;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.service.transaction.group_setting.GroupSettingDeleteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class GroupDeleteService {

    GroupReferRepository referRepository;
    GroupDeleteRepository deleteRepository;
    GroupSettingDeleteService userSettingDeleteService;

    public GroupDeleteService(GroupReferRepository referRepository,
                              GroupDeleteRepository deleteRepository,
                              GroupSettingDeleteService userSettingDeleteService
                             ) {
        this.referRepository = referRepository;
        this.deleteRepository = deleteRepository;
        this.userSettingDeleteService = userSettingDeleteService;
    }

    public DeleteResult invoke(GroupId groupId) {

        try {
            // 所属グループ未登録チェック
            List<GroupEntity> entities =  referRepository.find(groupId,null,null,null,null);
            if(entities.size() == 0){
                return DeleteResult.fromNotFoundErrorForRelatedDataNotRegistered(
                        ItemNames.所属グループ,
                        "group_id",
                        groupId.getValue()
                );
            }

            // TODO お付き合い帳削除
            // TODO コメント削除
            // TODO 確定データ削除
            // TODO 予算削除
            // TODO 購入データ削除
            // TODO 購入データ登録リスト削除
            // TODO 購入データ入力リスト削除
            // TODO 購入データ検索リスト削除
            // TODO 購入ラベル削除
            // TODO カテゴリー削除
            // TODO 所属グループメンバー削除

            // 所属グループ設定削除
            if(!userSettingDeleteService.invoke(groupId, null).getHttpStatus().equals(HttpStatus.OK)){
                return DeleteResult.fromInternalServerError(ItemNames.所属グループ設定);
            }

            return DeleteResult.fromSuccess(
                    ItemNames.所属グループ,
                    deleteRepository.delete(groupId)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return DeleteResult.fromInternalServerError(ItemNames.所属グループ);
        }
    }
}
