package com.turfbooking.app.service.impl;

import com.turfbooking.app.dto.BookingRequest;
import com.turfbooking.app.dto.BookingResponse;
import com.turfbooking.app.entity.Booking;
import com.turfbooking.app.entity.Turf;
import com.turfbooking.app.entity.User;
import com.turfbooking.app.entity.enums.BookingStatus;
import com.turfbooking.app.exception.BadRequestException;
import com.turfbooking.app.exception.ResourceNotFoundException;
import com.turfbooking.app.exception.SlotAlreadyBookedException;
import com.turfbooking.app.repository.BookingRepository;
import com.turfbooking.app.repository.TurfRepository;
import com.turfbooking.app.repository.UserRepository;
import com.turfbooking.app.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TurfRepository turfRepository;
    private final UserRepository userRepository;

    /**
     * CRITICAL CONCURRENCY-SAFE BOOKING METHOD.
     *
     * Uses SERIALIZABLE isolation level + PESSIMISTIC_WRITE lock on turf row.
     *
     * Flow:
     * 1. Acquire DB-level pessimistic write lock on the turf row
     * 2. Check for overlapping bookings atomically
     * 3. If no conflict, create booking with PENDING status
     * 4. If conflict, throw SlotAlreadyBookedException → 409 Conflict
     *
     * This ensures that two simultaneous booking requests for the same slot
     * cannot both succeed — only one will get the lock; the other will wait
     * and then fail the overlap check.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse createBooking(BookingRequest request, Long userId) {

        // Validate time range
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Step 1: Acquire PESSIMISTIC_WRITE lock on the turf row
        // This blocks any other transaction from reading/writing this turf row until we commit
        Turf turf = turfRepository.findByIdWithLock(request.getTurfId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Turf not found with id: " + request.getTurfId()));

        log.debug("Lock acquired on turf {} for booking attempt by user {}", request.getTurfId(), userId);

        // Step 2: Check for overlapping bookings
        // Overlap condition: existing.startTime < requested.endTime AND existing.endTime > requested.startTime
        boolean hasConflict = bookingRepository.existsOverlappingBooking(
                request.getTurfId(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (hasConflict) {
            log.warn("Slot conflict detected for turf {} on {} {}–{} by user {}",
                    request.getTurfId(), request.getDate(),
                    request.getStartTime(), request.getEndTime(), userId);
            throw new SlotAlreadyBookedException(
                    "This slot is already booked. Please select a different time.");
        }

        // Step 3: Retrieve user and calculate price
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Calculate duration in hours and total price
        long hours = ChronoUnit.HOURS.between(request.getStartTime(), request.getEndTime());
        BigDecimal totalPrice = turf.getPricePerHour().multiply(BigDecimal.valueOf(hours));

        // Step 4: Create booking with PENDING status
        Booking booking = Booking.builder()
                .user(user)
                .turf(turf)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} created with PENDING status for user {} on turf {}", saved.getId(), userId, turf.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Only the booking owner or admin can cancel
        if (!booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only cancel your own bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking {} cancelled by user {}", bookingId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.turfbooking.app.dto.SlotResponse> getBookedSlotsOptimized(Long turfId, LocalDate date) {
        return bookingRepository.findBookedSlotsOptimized(turfId, date);
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getName())
                .turfId(booking.getTurf().getId())
                .turfName(booking.getTurf().getName())
                .turfLocation(booking.getTurf().getLocation())
                .date(booking.getDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .build();
    }
}
