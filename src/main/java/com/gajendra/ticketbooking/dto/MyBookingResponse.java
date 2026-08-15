package com.gajendra.ticketbooking.dto;

import com.gajendra.ticketbooking.entity.Booking;

public class MyBookingResponse {

    private final Long bookingId;
    private final Long eventId;
    private final String eventName;
    private final String venue;
    private final String eventDateTime;
    private final String status;
    private final boolean paid;

    public MyBookingResponse(Booking booking) {
        this.bookingId = booking.getId();
        this.eventId = booking.getEvent().getId();
        this.eventName = booking.getEvent().getName();
        this.venue = booking.getEvent().getVenue();
        this.eventDateTime = booking.getEvent().getEventDateTime();
        this.status = booking.getStatus().name();
        this.paid = booking.isPaid();
    }

    public Long getBookingId() { return bookingId; }
    public Long getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getVenue() { return venue; }
    public String getEventDateTime() { return eventDateTime; }
    public String getStatus() { return status; }
    public boolean isPaid() { return paid; }
}