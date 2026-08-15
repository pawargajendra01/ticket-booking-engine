package com.gajendra.ticketbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gajendra.ticketbooking.datastructure.WaitlistRequest;
import com.gajendra.ticketbooking.dto.BookingRequest;
import com.gajendra.ticketbooking.dto.BookingResponse;
import com.gajendra.ticketbooking.dto.MyBookingResponse;
import com.gajendra.ticketbooking.service.BookingService;

import jakarta.validation.Valid;
@RestController
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/api/events/{eventId}/book")
    @ResponseStatus(HttpStatus.OK)
    public BookingResponse bookSeat(@PathVariable Long eventId, @Valid @RequestBody BookingRequest request) {
        return bookingService.bookSeat(eventId, request);
    }

    @PostMapping("/api/bookings/{bookingId}/cancel")
    public BookingResponse cancelBooking(@PathVariable Long bookingId) {
        return bookingService.cancelBooking(bookingId);
    }

    @GetMapping("/api/events/{eventId}/waitlist")
    public List<WaitlistRequest> getWaitlist(@PathVariable Long eventId) {
        return bookingService.getWaitlist(eventId);
    }

    @GetMapping("/api/bookings")
    public List<MyBookingResponse> getMyBookings(@RequestParam String userId) {
        return bookingService.getMyBookings(userId);
    }
}
