package com.gajendra.ticketbooking;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gajendra.ticketbooking.dto.BookingRequest;
import com.gajendra.ticketbooking.dto.BookingResponse;
import com.gajendra.ticketbooking.entity.Event;
import com.gajendra.ticketbooking.repository.EventRepository;
import com.gajendra.ticketbooking.service.BookingService;

@SpringBootTest
class ConcurrencyBookingTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EventRepository eventRepository;

    private Long eventId;
    private static final int CONCURRENT_REQUESTS = 25;

    @BeforeEach
    void setUp() {
        Event event = new Event("Single Seat Concert", 1);
        event = eventRepository.save(event);
        eventId = event.getId();
    }

    @Test
    void onlyOneRequestShouldBeConfirmed_whenManyThreadsRaceForOneSeat() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_REQUESTS);
        AtomicInteger confirmedCount = new AtomicInteger(0);
        AtomicInteger waitlistedCount = new AtomicInteger(0);

        List<Runnable> tasks = IntStream.range(0, CONCURRENT_REQUESTS)
                .mapToObj(i -> (Runnable) () -> {
                    try {
                        startLatch.await();
                        BookingRequest request = new BookingRequest();
                        request.setUserId("user-" + i);
                        request.setPriorityTier(5);

                        BookingResponse response = bookingService.bookSeat(eventId, request);

                        if ("CONFIRMED".equals(response.getStatus())) {
                            confirmedCount.incrementAndGet();
                        } else if ("WAITLISTED".equals(response.getStatus())) {
                            waitlistedCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                })
                .collect(Collectors.toList());

        tasks.forEach(executor::submit);
        startLatch.countDown();
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "All booking threads should complete within timeout");
        assertEquals(1, confirmedCount.get(), "Exactly one request should win the single seat");
        assertEquals(CONCURRENT_REQUESTS - 1, waitlistedCount.get(), "All other requests should be waitlisted");

        Event finalEvent = eventRepository.findById(eventId).orElseThrow();
        assertEquals(0, finalEvent.getAvailableSeats(), "No seats should remain after the race");
    }
}