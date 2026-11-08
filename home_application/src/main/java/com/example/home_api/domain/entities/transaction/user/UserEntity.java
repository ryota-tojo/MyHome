package com.example.home_api.domain.entities.transaction.user;

import com.example.home_api.domain.value_objects.ApproveDatetime;
import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.DeleteDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.user.*;
import lombok.Value;

@Value
public class UserEntity {
    UserId userId;
    UserPassword password;
    UserName userName;
    UserPermissionFlag permissionFlag;
    UserApproveFlag approveFlag;
    UserDeleteFlag deleteFlag;
    ApproveDatetime approveDatetime;
    DeleteDatetime deleteDatetime;
    CreateDatetime createDatetime;
    UpdateDatetime updateDatetime;

    public static UserEntity of(
            UserId userId,
            UserPassword userPassword,
            UserName userName,
            UserPermissionFlag userPermissionFlag,
            UserApproveFlag userApproveFlag,
            UserDeleteFlag userDeleteFlag,
            ApproveDatetime approveDatetime,
            DeleteDatetime deleteDatetime,
            CreateDatetime createDatetime,
            UpdateDatetime updateDatetime
    ) {
        return new UserEntity(
                userId,
                userPassword,
                userName,
                userPermissionFlag,
                userApproveFlag,
                userDeleteFlag,
                approveDatetime,
                deleteDatetime,
                createDatetime,
                updateDatetime
        );
    }
}
