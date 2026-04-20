package com.turfbooking.app.repository;

import com.turfbooking.app.entity.Turf;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurfRepository extends JpaRepository<Turf, Long> {

    List<Turf> findByOwnerId(Long ownerId);

    /**
     * Acquires a PESSIMISTIC_WRITE lock on the turf row.
     * This prevents concurrent bookings on the same turf.
     * Used in BookingService before slot overlap check.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Turf t WHERE t.id = :id")
    Optional<Turf> findByIdWithLock(@Param("id") Long id);
}
