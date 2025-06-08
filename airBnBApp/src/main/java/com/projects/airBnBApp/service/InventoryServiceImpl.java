package com.projects.airBnBApp.service;

import com.projects.airBnBApp.dto.HotelPriceDto;
import com.projects.airBnBApp.dto.HotelSearchRequest;
import com.projects.airBnBApp.entity.Inventory;
import com.projects.airBnBApp.entity.Room;
import com.projects.airBnBApp.repository.HotelMinPriceRepository;
import com.projects.airBnBApp.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;

    private final HotelMinPriceRepository hotelMinPriceRepository;

    @Override
    public void initializeRoomForAYear(Room room) {

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        for( ; !today.isAfter(endDate) ; today=today.plusDays(1) ){
            log.info("Inventory for Hotel : " + room.getHotel().getId() + " Room "+ room.getId() + " Date " + today  );
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();

            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        LocalDate today = LocalDate.now();
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Seach Hotels : In Service Impl  Method 1 : " + hotelSearchRequest);
        Pageable pageable = PageRequest.of(
                hotelSearchRequest.getPage() , hotelSearchRequest.getSize()
        );
        //log.info("Seach Hotels : In Service Impl  Method 2 : " + pageable);

        Long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate() , hotelSearchRequest.getEndDate()) + 1;

        //log.info("Seach Hotels : In Service Impl  Method 3 : " + dateCount);

        //long start = System.currentTimeMillis();

        Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate() , hotelSearchRequest.getEndDate() , hotelSearchRequest.getRoomsCount() , dateCount , pageable);
        //long end = System.currentTimeMillis();
        //log.info("Seach Hotels : In Service Impl  Method 4 : {} ", (end - start));
        //log.info("Seach Hotels : In Service Impl  Method 5 : " + hotelPage);

        return hotelPage.map((element) -> modelMapper.map(element , HotelPriceDto.class));
    }
}
