package com.projects.airBnBApp.service;

import com.projects.airBnBApp.dto.HotelPriceDto;
import com.projects.airBnBApp.dto.HotelSearchRequest;
import com.projects.airBnBApp.dto.InventoryDto;
import com.projects.airBnBApp.entity.Inventory;
import com.projects.airBnBApp.entity.Room;
import com.projects.airBnBApp.entity.User;
import com.projects.airBnBApp.exception.ResourceNotFoundException;
import com.projects.airBnBApp.repository.HotelMinPriceRepository;
import com.projects.airBnBApp.repository.InventoryRepository;
import com.projects.airBnBApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.projects.airBnBApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{
    private final RoomRepository roomRepository;

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

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {

        log.info("Getting All the inventory for room with room id : {}"+ roomId);

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room Not found with Room Id : {}"+ roomId));

        User user = getCurrentUser();

        if( !room.getHotel().getOwner().equals(user)){
            throw new AccessDeniedException("You are not the owner of the Room id : {} "+ roomId);
        }
        return inventoryRepository.findByRoomOrderByDate(room)
                .stream()
                .map((element) -> modelMapper.map(element , InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating All the inventory for room with room id : {} between date range : {}-{} ", roomId , updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate() );
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room Not found with Room Id : {}"+ roomId));
        User user = getCurrentUser();
        if( !room.getHotel().getOwner().equals(user)){
            throw new AccessDeniedException("You are not the owner of the Room id : {} "+ roomId);
        }

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId, updateInventoryRequestDto.getStartDate() , updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId , updateInventoryRequestDto.getStartDate() , updateInventoryRequestDto.getEndDate() ,updateInventoryRequestDto.getClosed() , updateInventoryRequestDto.getSurgeFactor());

    }
}
