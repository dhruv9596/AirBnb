package com.projects.airBnBApp.repository;

import com.projects.airBnBApp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room , Long> {
}
