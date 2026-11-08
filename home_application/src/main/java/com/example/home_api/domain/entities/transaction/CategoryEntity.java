package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.category.CategoryDisabledFlag;
import com.example.home_api.domain.value_objects.transaction.category.CategoryId;
import com.example.home_api.domain.value_objects.transaction.category.CategoryName;
import com.example.home_api.domain.value_objects.transaction.category.CategoryNo;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import lombok.Value;

@Value
public class CategoryEntity {
    CategoryId id;
    GroupId groupId;
    CategoryNo no;
    CategoryName name;
    CategoryDisabledFlag disabledFlag;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static CategoryEntity of(
            CategoryId id,
            GroupId groupId,
            CategoryNo no,
            CategoryName name,
            CategoryDisabledFlag disabledFlag,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new CategoryEntity(
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
