package com.gajendra.ticketbooking.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(Long eventId) {
        super("Event not found: id=" + eventId);
    }
}