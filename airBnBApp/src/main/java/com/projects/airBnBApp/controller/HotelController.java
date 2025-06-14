package com.projects.airBnBApp.controller;

import com.projects.airBnBApp.dto.BookingDto;
import com.projects.airBnBApp.dto.HotelDto;
import com.projects.airBnBApp.dto.HotelReportDto;
import com.projects.airBnBApp.service.BookingService;
import com.projects.airBnBApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto){

        log.info("Attempting to create a new Hotel : " + hotelDto.getName());
        HotelDto hotel = hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotel , HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId){
        log.info("Attempting to fetch Hotel : " + hotelId );
        HotelDto hotel = hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotel);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId , @RequestBody HotelDto hotelDto){
        log.info("Attempting to Update Hotel : " + hotelId );
        HotelDto hotel = hotelService.updateHotelById(hotelId , hotelDto);
        return ResponseEntity.ok(hotel);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId){
        log.info("Attempting to Delete Hotel : " + hotelId );
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}")
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId){
        log.info("Attempting to Actvate the hotel : " + hotelId );
        hotelService.activateHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels(){
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookingsByHotelId(@PathVariable Long hotelId){

        return ResponseEntity.ok(bookingService.getAllBookingsByHotelId(hotelId));
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId , @RequestParam(required = false) LocalDate startDate , @RequestParam(required = false) LocalDate endDate){
        if( startDate == null ) startDate = LocalDate.now().minusMonths(1);
        if( endDate == null ) endDate = LocalDate.now();
        return ResponseEntity.ok(bookingService.getHotelReport(hotelId,startDate , endDate));
    }



}
