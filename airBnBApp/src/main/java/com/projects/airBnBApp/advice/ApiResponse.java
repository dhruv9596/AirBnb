package com.projects.airBnBApp.advice;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@ToString
public class ApiResponse<T> {

    private LocalDateTime timeStamp;
    private T data;
    private ApiError apiError;

    public ApiResponse(){
        this.timeStamp = LocalDateTime.now();
    }

    public ApiResponse( T data){
        this();
        this.data = data;

    }

    public ApiResponse( ApiError error){
        this();
        this.apiError = error;

    }
}
