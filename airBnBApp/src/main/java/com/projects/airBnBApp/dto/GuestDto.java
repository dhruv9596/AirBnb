package com.projects.airBnBApp.dto;

import com.projects.airBnBApp.entity.User;
import com.projects.airBnBApp.entity.enums.Gender;
import lombok.Data;

@Data
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
