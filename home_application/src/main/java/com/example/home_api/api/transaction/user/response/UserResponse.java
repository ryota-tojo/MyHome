package com.example.home_api.api.transaction.user.response;

import com.example.home_api.api.transaction.user_setting.response.UserSettingResponse;
import com.example.home_api.domain.entities.transaction.user.UserAndSettingEntity;
import com.example.home_api.domain.entities.transaction.user.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class UserResponse {

    @JsonProperty("user_id")
    String userId;

    @JsonProperty("user_name")
    String userName;

    @JsonProperty("permission_flag")
    int permissionFlag;

    @JsonProperty("approve_flag")
    int approveFlag;

    @JsonProperty("delete_flag")
    int deleteFlag;

    @JsonProperty("approve_date")
    String approveDate;

    @JsonProperty("delete_date")
    String deleteDate;

    @JsonProperty("create_date")
    String createDate;

    @JsonProperty("update_date")
    String updateDate;

    @JsonProperty("settings")
    List<UserSettingResponse> settings;

    public static UserResponse of(UserAndSettingEntity entity) {
        return new UserResponse(
                entity.getUserEntity().getUserId().getValue(),
                entity.getUserEntity().getUserName().getValue(),
                entity.getUserEntity().getPermissionFlag().getValue(),
                entity.getUserEntity().getApproveFlag().getValue(),
                entity.getUserEntity().getDeleteFlag().getValue(),
                (entity.getUserEntity().getApproveDatetime()==null) ? null : entity.getUserEntity().getApproveDatetime().getValue().toString(),
                (entity.getUserEntity().getDeleteDatetime()==null) ? null : entity.getUserEntity().getDeleteDatetime().getValue().toString(),
                entity.getUserEntity().getCreateDatetime().getValue().toString(),
                entity.getUserEntity().getUpdateDatetime().getValue().toString(),
                entity.getSettingEntities().stream().map(UserSettingResponse::of).toList()
        );
    }
}
