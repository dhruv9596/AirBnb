package com.projects.airBnBApp.repository;

import com.projects.airBnBApp.entity.Hotel;
import com.projects.airBnBApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel , Long> {

    List<Hotel> findByOwner(User user);
}
