package com.gajendra.ticketbooking.dto;

import java.time.Instant;

import com.gajendra.ticketbooking.entity.Booking;

public class BookingResponse {

    private final Long bookingId;
    private final Long eventId;
    private final String userId;
    private final String status;
    private final int remainingSeats;
    private final Instant paymentDeadline;

    public BookingResponse(Booking booking, int remainingSeats) {
        this.bookingId = booking.getId();
        this.eventId = booking.getEvent().getId();
        this.userId = booking.getUserId();
        this.status = booking.getStatus().name();
        this.remainingSeats = remainingSeats;
        this.paymentDeadline = booking.getPaymentDeadline();
    }

    public Long getBookingId() { return bookingId; }
    public Long getEventId() { return eventId; }
    public String getUserId() { return userId; }
    public String getStatus() { return status; }
    public int getRemainingSeats() { return remainingSeats; }
    public Instant getPaymentDeadline() { return paymentDeadline; }
}