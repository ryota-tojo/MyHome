package com.example.home_api.domain.value_objects.transaction.communication;

import lombok.Value;

@Value
public class CommunicationId {
    int value;

    public static CommunicationId of(int value) {
        return new CommunicationId(value);
    }
}
