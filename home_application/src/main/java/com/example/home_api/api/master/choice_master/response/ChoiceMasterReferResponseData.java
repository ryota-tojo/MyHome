package com.example.home_api.api.master.choice_master.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class ChoiceMasterReferResponseData {

    @JsonProperty("choices")
    List<ChoiceMasterResponse> choices;

    public static ChoiceMasterReferResponseData of(List<ChoiceMasterResponse> responses) {
        return new ChoiceMasterReferResponseData(responses);
    }
}
