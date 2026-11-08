package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.shopping_label.ShoppingLabelDisabledFlag;
import com.example.home_api.domain.value_objects.transaction.shopping_label.ShoppingLabelId;
import com.example.home_api.domain.value_objects.transaction.shopping_label.ShoppingLabelName;
import com.example.home_api.domain.value_objects.transaction.shopping_label.ShoppingLabelNo;
import lombok.Value;

@Value
public class ShoppingLabelEntity {
    ShoppingLabelId id;
    GroupId groupId;
    ShoppingLabelNo no;
    ShoppingLabelName name;
    ShoppingLabelDisabledFlag disabledFlag;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static ShoppingLabelEntity of(
            ShoppingLabelId id,
            GroupId groupId,
            ShoppingLabelNo no,
            ShoppingLabelName name,
            ShoppingLabelDisabledFlag disabledFlag,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new ShoppingLabelEntity(
                id,
                groupId,
                no,
                name,
                disabledFlag,
                createDatetime,
                updateDatetime
        );
    }
}
