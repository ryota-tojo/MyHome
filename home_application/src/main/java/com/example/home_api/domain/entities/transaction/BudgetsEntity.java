package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.Month;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.Year;
import com.example.home_api.domain.value_objects.transaction.category.CategoryId;
import com.example.home_api.domain.value_objects.transaction.fixed.FixedFlag;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.shopping.Amount;
import lombok.Value;

@Value
public class BudgetsEntity {
    GroupId groupId;
    Year year;
    Month month;
    CategoryId categoryId;
    Amount amount;
    FixedFlag fixedFlag;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static BudgetsEntity of(
            GroupId groupId,
            Year year,
            Month month,
            CategoryId categoryId,
            Amount amount,
            FixedFlag fixedFlag,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new BudgetsEntity(
                groupId,
                year,
                month,
                categoryId,
                amount,
                fixedFlag,
                createDatetime,
                updateDatetime
        );
    }
}
