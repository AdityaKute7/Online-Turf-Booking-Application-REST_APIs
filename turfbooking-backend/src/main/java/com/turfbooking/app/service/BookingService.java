package com.turfbooking.app.service;

import com.turfbooking.app.dto.BookingRequest;
import com.turfbooking.app.dto.BookingResponse;


import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request, Long userId);

    List<BookingResponse> getUserBookings(Long userId);

    void cancelBooking(Long bookingId, Long userId);

    List<com.turfbooking.app.dto.SlotResponse> getBookedSlotsOptimized(Long turfId, LocalDate date);
}
