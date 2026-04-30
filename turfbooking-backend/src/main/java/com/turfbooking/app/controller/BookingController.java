package com.turfbooking.app.controller;

import com.turfbooking.app.dto.ApiResponse;
import com.turfbooking.app.dto.BookingRequest;
import com.turfbooking.app.dto.BookingResponse;
import com.turfbooking.app.entity.User;
import com.turfbooking.app.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Creates a new booking with PENDING status.
     * Concurrency-safe — uses pessimistic locking to prevent double-booking.
     */
    @PostMapping("/create")
    @Operation(summary = "Create a new booking (concurrency-safe)")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal User currentUser) {

        BookingResponse booking = bookingService.createBooking(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created with PENDING status", booking));
    }

    @GetMapping("/user")
    @Operation(summary = "Get all bookings for the current user")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUserBookings(
            @AuthenticationPrincipal User currentUser) {

        List<BookingResponse> bookings = bookingService.getUserBookings(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User bookings fetched", bookings));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        bookingService.cancelBooking(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", null));
    }

    @GetMapping("/slots")
    @Operation(summary = "Get booked slots for a turf on a specific date")
    public ResponseEntity<ApiResponse<List<com.turfbooking.app.dto.SlotResponse>>> getBookedSlots(
            @RequestParam Long turfId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<com.turfbooking.app.dto.SlotResponse> slots = bookingService.getBookedSlotsOptimized(turfId, date);
        return ResponseEntity.ok(ApiResponse.success("Booked slots fetched", slots));
    }
}
