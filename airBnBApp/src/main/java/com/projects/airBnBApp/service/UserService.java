package com.projects.airBnBApp.service;

import com.projects.airBnBApp.dto.ProfileUpdateRequestDto;
import com.projects.airBnBApp.dto.UserDto;
import com.projects.airBnBApp.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
