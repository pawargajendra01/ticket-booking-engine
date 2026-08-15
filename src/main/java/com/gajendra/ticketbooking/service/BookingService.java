package com.gajendra.ticketbooking.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.gajendra.ticketbooking.datastructure.WaitlistRequest;
import com.gajendra.ticketbooking.dto.BookingRequest;
import com.gajendra.ticketbooking.dto.BookingResponse;
import com.gajendra.ticketbooking.dto.MyBookingResponse;
import com.gajendra.ticketbooking.entity.Booking;
import com.gajendra.ticketbooking.entity.BookingStatus;
import com.gajendra.ticketbooking.entity.Event;
import com.gajendra.ticketbooking.exception.BookingNotFoundException;
import com.gajendra.ticketbooking.exception.EventNotFoundException;
import com.gajendra.ticketbooking.exception.InvalidBookingStateException;
import com.gajendra.ticketbooking.repository.BookingRepository;
import com.gajendra.ticketbooking.repository.EventRepository;

@Service
public class BookingService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistManager waitlistManager;

    private static final long PAYMENT_DEADLINE_SECONDS = 600; // 10 minutes to pay

    public BookingService(EventRepository eventRepository,
            BookingRepository bookingRepository,
            WaitlistManager waitlistManager) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.waitlistManager = waitlistManager;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingResponse bookSeat(Long eventId, BookingRequest request) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.hasSeatAvailable()) {
            event.decrementSeat();
            eventRepository.save(event);

            Booking booking = new Booking(event, request.getUserId(), request.getPriorityTier(), BookingStatus.CONFIRMED);
            booking.setPaymentDeadline(Instant.now().plusSeconds(PAYMENT_DEADLINE_SECONDS));
            bookingRepository.save(booking);

            return new BookingResponse(booking, event.getAvailableSeats());
        }

        Booking waitlistedBooking = new Booking(event, request.getUserId(), request.getPriorityTier(), BookingStatus.WAITLISTED);
        bookingRepository.save(waitlistedBooking);

        WaitlistRequest waitlistRequest = new WaitlistRequest(
                waitlistedBooking.getId(),
                waitlistedBooking.getUserId(),
                waitlistedBooking.getPriorityTier(),
                waitlistedBooking.getRequestedAt()
        );
        waitlistManager.addToWaitlist(eventId, waitlistRequest);

        return new BookingResponse(waitlistedBooking, event.getAvailableSeats());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Booking " + bookingId + " is already cancelled");
        }

        Long eventId = booking.getEvent().getId();
        boolean wasConfirmedSeat = booking.getStatus() == BookingStatus.CONFIRMED;
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        if (!wasConfirmedSeat) {
            Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
            return new BookingResponse(booking, event.getAvailableSeats());
        }

        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        event.incrementSeat();

        WaitlistRequest next = waitlistManager.pollNext(eventId);
        if (next != null) {
            Booking promoted = bookingRepository.findById(next.getBookingId())
                    .orElseThrow(() -> new BookingNotFoundException(next.getBookingId()));
            promoted.setStatus(BookingStatus.CONFIRMED);
            event.decrementSeat();
            bookingRepository.save(promoted);
        }

        eventRepository.save(event);
        return new BookingResponse(booking, event.getAvailableSeats());
    }

    @Transactional(readOnly = true)
    public List<WaitlistRequest> getWaitlist(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(eventId);
        }
        return waitlistManager.viewWaitlist(eventId);
    }

    @Transactional(readOnly = true)
    public List<MyBookingResponse> getMyBookings(String userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream().map(MyBookingResponse::new).collect(Collectors.toList());
    }
}
