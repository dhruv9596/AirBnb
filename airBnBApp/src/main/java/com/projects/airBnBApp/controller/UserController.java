package com.projects.airBnBApp.controller;

import com.projects.airBnBApp.dto.BookingDto;
import com.projects.airBnBApp.dto.ProfileUpdateRequestDto;
import com.projects.airBnBApp.dto.UserDto;
import com.projects.airBnBApp.service.BookingService;
import com.projects.airBnBApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){

        userService.updateProfile(profileUpdateRequestDto);

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDto>> getMyBookings(){
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getMyProfile(){
        return ResponseEntity.ok(userService.getMyProfile());
    }

}
