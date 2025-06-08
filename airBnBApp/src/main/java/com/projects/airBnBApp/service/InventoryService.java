package com.projects.airBnBApp.service;

import com.projects.airBnBApp.dto.HotelDto;
import com.projects.airBnBApp.dto.HotelPriceDto;
import com.projects.airBnBApp.dto.HotelSearchRequest;
import com.projects.airBnBApp.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeRoomForAYear( Room room );

    void deleteAllInventories( Room room );

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
