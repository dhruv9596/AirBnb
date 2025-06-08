package com.projects.airBnBApp.repository;

import com.projects.airBnBApp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking , Long> {
}
