package com.projects.airBnBApp.controller;
import com.projects.airBnBApp.dto.RoomDto;
import com.projects.airBnBApp.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@Slf4j
public class RoomAdminController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId , @RequestBody RoomDto roomDto){
        log.info("Create a New Room : " + roomDto);
        RoomDto room = roomService.createNewRoom(hotelId , roomDto);
        return new ResponseEntity<>(room , HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRoomsInHotel( @PathVariable Long hotelId ){
        List<RoomDto> rooms = roomService.getAllRoomsInHotel(hotelId);
        return new ResponseEntity<>( rooms , HttpStatus.OK);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long hotelId , @PathVariable Long roomId ){
        return new ResponseEntity<>( roomService.getRoomById(roomId) , HttpStatus.OK);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById( @PathVariable Long hotelId , @PathVariable Long roomId ){
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }
}
