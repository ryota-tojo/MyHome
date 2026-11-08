package com.example.home_api.domain.repositories.transaction.user;

import com.example.home_api.domain.value_objects.transaction.user.*;

public interface UserCreateRepository {

    String create(UserId userId, UserPassword userPassword, UserName userName, UserPermissionFlag userPermissionFlag, UserApproveFlag userApproveFlag, UserDeleteFlag userDeleteFlag);

}
