package com.turfbooking.app.repository;

import com.turfbooking.app.entity.Booking;
import com.turfbooking.app.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"user", "turf"})
    List<Booking> findByUserId(Long userId);

    List<Booking> findByTurfId(Long turfId);

    /**
     * CRITICAL CONCURRENCY CHECK:
     * Checks if any booking exists that overlaps with the requested time slot.
     *
     * Overlap condition:
     *   existing.startTime < requested.endTime AND existing.endTime > requested.startTime
     *
     * Only checks PENDING and CONFIRMED bookings (not CANCELLED).
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.turf.id = :turfId " +
           "AND b.date = :date " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    boolean existsOverlappingBooking(
            @Param("turfId") Long turfId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Get all confirmed/pending bookings for a turf on a specific date.
     * Used by frontend to display unavailable slots.
     */

    @Query("SELECT new com.turfbooking.app.dto.SlotResponse(b.startTime, b.endTime) FROM Booking b " +
            "WHERE b.turf.id = :turfId " +
            "AND b.date = :date " +
            "AND b.status IN ('PENDING', 'CONFIRMED')")
    List<com.turfbooking.app.dto.SlotResponse> findBookedSlotsOptimized(
            @Param("turfId") Long turfId,
            @Param("date") LocalDate date
    );

    List<Booking> findByTurfIdAndStatus(Long turfId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :cutoffTime")
    List<Booking> findExpiredBookings(
            @Param("status") BookingStatus status,
            @Param("cutoffTime") java.time.LocalDateTime cutoffTime
    );
}
