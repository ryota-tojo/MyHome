package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.category.CategoryId;
import com.example.home_api.domain.value_objects.transaction.fixed.FixedFlag;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.shopping.*;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import lombok.Value;

@Value
public class ShoppingDataEntity {
    ShoppingId id;
    GroupId groupId;
    ShoppingDate shoppingDate;
    UserId shoppingUserId;
    CategoryId categoryId;
    ShoppingType type;
    ShoppingPayment payment;
    ShoppingSettlement settlement;
    Amount amount;
    ShoppingLabelIds labelIds;
    ShoppingRemarks remarks;
    UserId createUserId;
    FixedFlag fixedFlag;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static ShoppingDataEntity of(
            ShoppingId id,
            GroupId groupId,
            ShoppingDate shoppingDate,
            UserId shoppingUserId,
            CategoryId categoryId,
            ShoppingType type,
            ShoppingPayment payment,
            ShoppingSettlement settlement,
            Amount amount,
            ShoppingLabelIds labelIds,
            ShoppingRemarks remarks,
            UserId createUserId,
            FixedFlag fixedFlag,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new ShoppingDataEntity(
                id,
                groupId,
                shoppingDate,
                shoppingUserId,
                categoryId,
                type,
                payment,
                settlement,
                amount,
                labelIds,
                remarks,
                createUserId,
                fixedFlag,
                createDatetime,
                updateDatetime
        );
    }
}
