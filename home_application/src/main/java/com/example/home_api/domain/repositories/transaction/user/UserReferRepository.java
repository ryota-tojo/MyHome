package com.example.home_api.domain.repositories.transaction.user;

import com.example.home_api.domain.common.Limit;
import com.example.home_api.domain.common.Offset;
import com.example.home_api.domain.entities.transaction.user.UserEntity;
import com.example.home_api.domain.value_objects.transaction.user.*;

import java.util.List;

public interface UserReferRepository {

    List<UserEntity> find(UserId userId, UserPassword userPassword,UserName userName, UserPermissionFlag permissionFlag, UserApproveFlag approveFlag, UserDeleteFlag deleteFlag, Offset offset, Limit limit);

}
