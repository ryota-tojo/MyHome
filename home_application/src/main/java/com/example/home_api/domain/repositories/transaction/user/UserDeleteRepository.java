package com.example.home_api.domain.repositories.transaction.user;

import com.example.home_api.domain.value_objects.transaction.user.UserId;

public interface UserDeleteRepository {

    Integer delete(UserId userId);

}
