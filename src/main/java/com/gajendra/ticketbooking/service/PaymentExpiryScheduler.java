package com.gajendra.ticketbooking.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gajendra.ticketbooking.entity.Booking;
import com.gajendra.ticketbooking.entity.BookingStatus;
import com.gajendra.ticketbooking.repository.BookingRepository;

@Component
public class PaymentExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentExpiryScheduler.class);

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public PaymentExpiryScheduler(BookingRepository bookingRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    @Scheduled(fixedRate = 30000)
    public void releaseExpiredUnpaidSeats() {
        List<Booking> expired = bookingRepository.findByStatusAndPaidFalseAndPaymentDeadlineBefore(
                BookingStatus.CONFIRMED, Instant.now());

        for (Booking booking : expired) {
            log.info("Releasing expired unpaid booking id={} for user={}", booking.getId(), booking.getUserId());
            bookingService.cancelBooking(booking.getId());
        }
    }
}