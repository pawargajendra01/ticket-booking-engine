package com.gajendra.ticketbooking.datastructure;

import java.time.Instant;

public class WaitlistRequest implements Comparable<WaitlistRequest> {

    private final Long bookingId;
    private final String userId;
    private final int priorityTier;
    private final Instant requestedAt;

    public WaitlistRequest(Long bookingId, String userId, int priorityTier, Instant requestedAt) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.priorityTier = priorityTier;
        this.requestedAt = requestedAt;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public int getPriorityTier() {
        return priorityTier;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    @Override
    public int compareTo(WaitlistRequest other) {
        int tierCompare = Integer.compare(this.priorityTier, other.priorityTier);
        if (tierCompare != 0) {
            return tierCompare;
        }
        return this.requestedAt.compareTo(other.requestedAt);
    }
}