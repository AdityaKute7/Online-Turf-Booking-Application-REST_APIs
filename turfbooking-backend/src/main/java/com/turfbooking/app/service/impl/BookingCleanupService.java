package com.turfbooking.app.service.impl;

import com.turfbooking.app.entity.Booking;
import com.turfbooking.app.entity.enums.BookingStatus;
import com.turfbooking.app.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCleanupService {

    private final BookingRepository bookingRepository;

    /**
     * Runs every 1 minute.
     * Finds and cancels any PENDING bookings that are older than 10 minutes.
     * This automatically releases slots if the user closes the browser or forgets to pay.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(BookingStatus.PENDING, cutoff);

        if (!expiredBookings.isEmpty()) {
            log.info("Found {} expired PENDING bookings. Cancelling them to release slots...", expiredBookings.size());
            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.CANCELLED);
            }
            bookingRepository.saveAll(expiredBookings);
        }
    }
}
