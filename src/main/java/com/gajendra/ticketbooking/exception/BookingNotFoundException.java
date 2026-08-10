package com.gajendra.ticketbooking.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long bookingId) {
        super("Booking not found: id=" + bookingId);
    }
}