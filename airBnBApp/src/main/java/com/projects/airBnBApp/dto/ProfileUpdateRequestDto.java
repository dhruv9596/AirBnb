package com.projects.airBnBApp.dto;

import com.projects.airBnBApp.entity.enums.Gender;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {

    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;

}
