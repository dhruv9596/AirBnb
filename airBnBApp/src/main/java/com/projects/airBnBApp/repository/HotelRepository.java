package com.projects.airBnBApp.repository;

import com.projects.airBnBApp.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel , Long> {



}
