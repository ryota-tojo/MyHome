package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.category.CategoryId;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.lists.shopping_search_list.ShoppingSearchListDisabledFlag;
import com.example.home_api.domain.value_objects.transaction.lists.shopping_search_list.ShoppingSearchListId;
import com.example.home_api.domain.value_objects.transaction.lists.shopping_search_list.ShoppingSearchListName;
import com.example.home_api.domain.value_objects.transaction.shopping.*;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import lombok.Value;

@Value
public class ShoppingSearchListEntity {
    ShoppingSearchListId id;
    GroupId groupId;
    ShoppingSearchListName listName;
    ShoppingDate fromShoppingDate;
    ShoppingDate toShoppingDate;
    UserId shoppingUserId;
    CategoryId categoryId;
    ShoppingType type;
    ShoppingPayment payment;
    ShoppingSettlement settlement;
    Amount minAmount;
    Amount maxAmount;
    ShoppingLabelIds labelIds;
    ShoppingRemarks remarks;
    ShoppingSearchListDisabledFlag disabledFlag;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static ShoppingSearchListEntity of(
            ShoppingSearchListId id,
            GroupId groupId,
            ShoppingSearchListName listName,
            ShoppingDate fromShoppingDate,
            ShoppingDate toShoppingDate,
            UserId shoppingUserId,
            CategoryId categoryId,
            ShoppingType type,
            ShoppingPayment payment,
            ShoppingSettlement settlement,
            Amount minAmount,
            Amount maxAmount,
            ShoppingLabelIds labelIds,
            ShoppingRemarks remarks,
            ShoppingSearchListDisabledFlag disabledFlag,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new ShoppingSearchListEntity(
                id,
                groupId,
                listName,
                fromShoppingDate,
                toShoppingDate,
                shoppingUserId,
                categoryId,
                type,
                payment,
                settlement,
                minAmount,
                maxAmount,
                labelIds,
                remarks,
                disabledFlag,
                createDatetime,
                updateDatetime
        );
    }
}
