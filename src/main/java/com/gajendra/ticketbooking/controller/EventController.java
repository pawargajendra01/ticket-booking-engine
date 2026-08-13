package com.gajendra.ticketbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gajendra.ticketbooking.entity.BookingStatus;
import com.gajendra.ticketbooking.entity.Event;
import com.gajendra.ticketbooking.exception.EventNotFoundException;
import com.gajendra.ticketbooking.exception.InvalidBookingStateException;
import com.gajendra.ticketbooking.repository.BookingRepository;
import com.gajendra.ticketbooking.repository.EventRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public EventController(EventRepository eventRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    public static class CreateEventRequest {

        @NotBlank
        public String name;
        @Min(1)
        public int totalSeats;
        @NotBlank
        public String venue;
        @NotBlank
        public String eventDateTime;
        public String description;
        public String imageUrl;
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event event = new Event(request.name, request.totalSeats, request.venue, request.eventDateTime, request.description, request.imageUrl);
        return eventRepository.save(event);
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException(id);
        }
        long activeBookings = bookingRepository.countByEventIdAndStatusIn(id, List.of(BookingStatus.CONFIRMED, BookingStatus.WAITLISTED));
        if (activeBookings > 0) {
            throw new InvalidBookingStateException("Cannot delete event with active bookings");
        }
        eventRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
