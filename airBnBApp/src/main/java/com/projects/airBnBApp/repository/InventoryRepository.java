package com.projects.airBnBApp.repository;

import com.projects.airBnBApp.entity.Hotel;
import com.projects.airBnBApp.entity.Inventory;
import com.projects.airBnBApp.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory , Long> {

        void deleteByRoom( Room room);

        @Query("""
           SELECT i.hotel
           FROM Inventory i
           WHERE i.city = :city
             AND i.date BETWEEN :startDate AND :endDate
             AND i.closed = false
             AND (i.totalCount - i.bookedCount -i.reservedCount) >= :roomsCount
           GROUP BY i.hotel
           HAVING COUNT(DISTINCT i.date) = :dateCount
            """)
        Page<Hotel> findHotelsWithAvailableWithInventory(
                @Param("city") String city,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("roomsCount") Integer roomsCount,
                @Param("dateCount") Long dateCount,
                Pageable pageable
        );

        @Query("""
                SELECT i FROM Inventory i
                WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed = false
                AND (i.totalCount - i.bookedCount-i.reservedCount) >= :roomsCount
                """)
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        List<Inventory> findAndLockInventory(
                @Param("roomId") Long roomId,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("roomsCount") Integer roomsCount

        );

        @Query("""
                SELECT i
                FROM Inventory i
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                  AND (i.totalCount - i.bookedCount) >= :numberOfRooms
                  AND i.closed = false
            """)
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        List<Inventory> findAndLockReservedInventory(@Param("roomId") Long roomId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     @Param("numberOfRooms") int numberOfRooms);


        @Modifying
        @Query("""
                UPDATE Inventory i
                SET i.reservedCount = i.reservedCount + :numberOfRooms
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                  AND (i.totalCount - i.bookedCount - i.reservedCount) >= :numberOfRooms
                  AND i.closed = false
            """)
        void initBooking(@Param("roomId") Long roomId,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate,
                         @Param("numberOfRooms") int numberOfRooms);


        @Modifying
        @Query("""
                UPDATE Inventory i
                SET i.reservedCount = i.reservedCount - :numberOfRooms,
                    i.bookedCount = i.bookedCount + :numberOfRooms
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                  AND (i.totalCount - i.bookedCount) >= :numberOfRooms
                  AND i.reservedCount >= :numberOfRooms
                  AND i.closed = false
            """)
        void confirmBooking(@Param("roomId") Long roomId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate,
                            @Param("numberOfRooms") int numberOfRooms);


        @Modifying
        @Query("""
                UPDATE Inventory i
                SET i.bookedCount = i.bookedCount - :numberOfRooms
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                  AND (i.totalCount - i.bookedCount) >= :numberOfRooms
                  AND i.closed = false
            """)
        void cancelBooking(@Param("roomId") Long roomId,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate,
                           @Param("numberOfRooms") int numberOfRooms);


        List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);



}
