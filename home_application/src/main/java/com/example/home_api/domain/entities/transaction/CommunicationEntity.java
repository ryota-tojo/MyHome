package com.example.home_api.domain.entities.transaction;

import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.communication.*;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import lombok.Value;

@Value
public class CommunicationEntity {
    CommunicationId id;
    GroupId groupId;
    ReceiveDate receiveDate;
    ReceiveFrom receiveFrom;
    ReceiveTo receiveTo;
    ReceiveItem receiveItem;
    ReceiveAmount receiveAmount;
    ReceiveRemarks receiveRemarks;
    ReturnDate returnDate;
    ReturnItem returnItem;
    ReturnAmount returnAmount;
    ReturnRemarks returnRemarks;
    ReturnFlag returnFlag;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static CommunicationEntity of(
            CommunicationId id,
            GroupId groupId,
            ReceiveDate receiveDate,
            ReceiveFrom receiveFrom,
            ReceiveTo receiveTo,
            ReceiveItem receiveItem,
            ReceiveAmount receiveAmount,
            ReceiveRemarks receiveRemarks,
            ReturnDate returnDate,
            ReturnItem returnItem,
            ReturnAmount returnAmount,
            ReturnRemarks returnRemarks,
            ReturnFlag returnFlag,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new CommunicationEntity(
                id,
                groupId,
                receiveDate,
                receiveFrom,
                receiveTo,
                receiveItem,
                receiveAmount,
                receiveRemarks,
                returnDate,
                returnItem,
                returnAmount,
                returnRemarks,
                returnFlag,
                createDatetime,
                updateDatetime
        );
    }
}
