package com.gajendra.ticketbooking.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gajendra.ticketbooking.entity.Booking;
import com.gajendra.ticketbooking.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByEventId(Long eventId);
    List<Booking> findByStatusAndPaidFalseAndPaymentDeadlineBefore(BookingStatus status, Instant deadline);
}
