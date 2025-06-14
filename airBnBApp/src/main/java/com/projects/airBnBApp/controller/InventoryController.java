package com.projects.airBnBApp.controller;


import com.projects.airBnBApp.dto.InventoryDto;
import com.projects.airBnBApp.dto.RoomDto;
import com.projects.airBnBApp.service.InventoryService;
import com.projects.airBnBApp.service.UpdateInventoryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<InventoryDto>> getAllInvetory(@PathVariable Long roomId){
        return ResponseEntity.ok(inventoryService.getAllInventoryByRoom(roomId));

    }

    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<Void> updateInventoryById(@PathVariable Long roomId , @RequestBody UpdateInventoryRequestDto updateInventoryRequestDto){

        inventoryService.updateInventory(roomId , updateInventoryRequestDto);
        return ResponseEntity.noContent().build();

    }

}
