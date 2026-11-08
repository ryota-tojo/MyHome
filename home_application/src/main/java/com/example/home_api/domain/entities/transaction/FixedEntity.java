package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.Month;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.Year;
import com.example.home_api.domain.value_objects.transaction.fixed.FixedRemarks;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import lombok.Value;

@Value
public class FixedEntity {
    GroupId groupId;
    Year year;
    Month month;
    FixedRemarks remarks;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static FixedEntity of(
            GroupId groupId,
            Year year,
            Month month,
            FixedRemarks remarks,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new FixedEntity(
                groupId,
                year,
                month,
                remarks,
                createDatetime,
                updateDatetime
        );
    }
}
