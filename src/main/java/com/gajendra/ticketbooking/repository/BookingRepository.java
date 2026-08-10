package com.gajendra.ticketbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gajendra.ticketbooking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByEventId(Long eventId);
}
