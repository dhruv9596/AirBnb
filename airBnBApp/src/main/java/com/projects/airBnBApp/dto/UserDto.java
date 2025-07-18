package com.projects.airBnBApp.dto;
import com.projects.airBnBApp.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
