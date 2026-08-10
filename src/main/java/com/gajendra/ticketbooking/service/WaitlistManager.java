package com.gajendra.ticketbooking.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.gajendra.ticketbooking.datastructure.CustomPriorityQueue;
import com.gajendra.ticketbooking.datastructure.WaitlistRequest;

@Component
public class WaitlistManager {

    private final Map<Long, CustomPriorityQueue<WaitlistRequest>> waitlistsByEvent = new ConcurrentHashMap<>();

    private CustomPriorityQueue<WaitlistRequest> queueFor(Long eventId) {
        return waitlistsByEvent.computeIfAbsent(eventId, id -> new CustomPriorityQueue<>());
    }

    public void addToWaitlist(Long eventId, WaitlistRequest request) {
        CustomPriorityQueue<WaitlistRequest> queue = queueFor(eventId);
        synchronized (queue) {
            queue.insert(request);
        }
    }

    public WaitlistRequest pollNext(Long eventId) {
        CustomPriorityQueue<WaitlistRequest> queue = queueFor(eventId);
        synchronized (queue) {
            if (queue.isEmpty()) {
                return null;
            }
            return queue.extractMin();
        }
    }

    public List<WaitlistRequest> viewWaitlist(Long eventId) {
        CustomPriorityQueue<WaitlistRequest> queue = queueFor(eventId);
        synchronized (queue) {
            return queue.toSortedList();
        }
    }

    public int waitlistSize(Long eventId) {
        CustomPriorityQueue<WaitlistRequest> queue = queueFor(eventId);
        synchronized (queue) {
            return queue.size();
        }
    }
}