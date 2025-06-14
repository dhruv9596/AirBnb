package com.projects.airBnBApp.controller;


import com.projects.airBnBApp.dto.BookingDto;
import com.projects.airBnBApp.dto.BookingRequest;
import com.projects.airBnBApp.dto.GuestDto;
import com.projects.airBnBApp.entity.Guest;
import com.projects.airBnBApp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequest bookingRequest){

        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));

    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(
            @PathVariable Long bookingId,
            @RequestBody List<GuestDto> guestDtoList){

        return ResponseEntity.ok(bookingService.addGuests(bookingId , guestDtoList));

    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String , String>> initiatePayments(@PathVariable Long bookingId){

        String sessionUrl =  bookingService.initiateBooking(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl" , sessionUrl));

    }


    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/status")
    public ResponseEntity<Map<String, String>> getBookingStatus(
            @PathVariable Long bookingId){
        return ResponseEntity.ok(
                Map.of("status" ,
                bookingService.getBookingStatus(bookingId)));
    }
}