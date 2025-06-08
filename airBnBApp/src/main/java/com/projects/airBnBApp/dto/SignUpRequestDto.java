package com.projects.airBnBApp.dto;


import lombok.Data;

@Data
public class SignUpRequestDto {

    private String name , email,password;
    private Long id;

}
