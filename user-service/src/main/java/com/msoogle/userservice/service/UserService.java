package com.msoogle.userservice.service;

import com.msoogle.userservice.dto.UserDto;
import com.msoogle.userservice.jpa.UserEntity;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto getUserByUserId(String userId);

    Iterable<UserEntity> getUserByAll();
}
