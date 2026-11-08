package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.category.CategoryId;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.lists.shopping_input_list.ShoppingInputListDisabledFlag;
import com.example.home_api.domain.value_objects.transaction.lists.shopping_input_list.ShoppingInputListId;
import com.example.home_api.domain.value_objects.transaction.lists.shopping_input_list.ShoppingInputListName;
import com.example.home_api.domain.value_objects.transaction.shopping.*;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import lombok.Value;

@Value
public class ShoppingInputListEntity {
    ShoppingInputListId id;
    GroupId groupId;
    ShoppingInputListName listName;
    ShoppingDate shoppingDate;
    UserId shoppingUserId;
    CategoryId categoryId;
    ShoppingType type;
    ShoppingPayment payment;
    ShoppingSettlement settlement;
    Amount amount;
    ShoppingLabelIds labelIds;
    ShoppingRemarks remarks;
    ShoppingInputListDisabledFlag disabledFlag;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static ShoppingInputListEntity of(
            ShoppingInputListId id,
            GroupId groupId,
            ShoppingInputListName listName,
            ShoppingDate shoppingDate,
            UserId shoppingUserId,
            CategoryId categoryId,
            ShoppingType type,
            ShoppingPayment payment,
            ShoppingSettlement settlement,
            Amount amount,
            ShoppingLabelIds labelIds,
            ShoppingRemarks remarks,
            ShoppingInputListDisabledFlag disabledFlag,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new ShoppingInputListEntity(
                id,
                groupId,
                listName,
                shoppingDate,
                shoppingUserId,
                categoryId,
                type,
                payment,
                settlement,
                amount,
                labelIds,
                remarks,
                disabledFlag,
                createDatetime,
                updateDatetime
        );
    }
}
