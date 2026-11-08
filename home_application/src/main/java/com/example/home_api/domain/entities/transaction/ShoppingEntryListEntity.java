package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.category.CategoryId;
import com.example.home_api.domain.value_objects.transaction.fixed.FixedFlag;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.lists.shopping_entry_list.ShoppingEntryListDisabledFlag;
import com.example.home_api.domain.value_objects.transaction.lists.shopping_entry_list.ShoppingEntryListId;
import com.example.home_api.domain.value_objects.transaction.lists.shopping_entry_list.ShoppingEntryListName;
import com.example.home_api.domain.value_objects.transaction.shopping.*;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import lombok.Value;

@Value
public class ShoppingEntryListEntity {
    ShoppingEntryListId id;
    GroupId groupId;
    ShoppingEntryListName listName;
    ShoppingDate shoppingDate;
    UserId shoppingUserId;
    CategoryId categoryId;
    ShoppingType type;
    ShoppingPayment payment;
    ShoppingSettlement settlement;
    Amount amount;
    ShoppingLabelIds labelIds;
    ShoppingRemarks remarks;
    ShoppingEntryListDisabledFlag disabledFlag;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static ShoppingEntryListEntity of(
            ShoppingEntryListId id,
            GroupId groupId,
            ShoppingEntryListName listName,
            ShoppingDate shoppingDate,
            UserId shoppingUserId,
            CategoryId categoryId,
            ShoppingType type,
            ShoppingPayment payment,
            ShoppingSettlement settlement,
            Amount amount,
            ShoppingLabelIds labelIds,
            ShoppingRemarks remarks,
            ShoppingEntryListDisabledFlag disabledFlag,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new ShoppingEntryListEntity(
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
