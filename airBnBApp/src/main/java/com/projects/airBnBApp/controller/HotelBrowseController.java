package com.projects.airBnBApp.controller;


import com.projects.airBnBApp.dto.HotelDto;
import com.projects.airBnBApp.dto.HotelInfoDto;
import com.projects.airBnBApp.dto.HotelPriceDto;
import com.projects.airBnBApp.dto.HotelSearchRequest;
import com.projects.airBnBApp.service.HotelService;
import com.projects.airBnBApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){
        log.info("Search Hotels : " + hotelSearchRequest);
        Page<HotelPriceDto> page = inventoryService.searchHotels( hotelSearchRequest );
        return ResponseEntity.ok(page);

    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelinfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }

}
