package com.gajendra.ticketbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BookingRequest {

    @NotBlank
    private String userId;

    @Min(1)
    private int priorityTier = 5;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPriorityTier() {
        return priorityTier;
    }

    public void setPriorityTier(int priorityTier) {
        this.priorityTier = priorityTier;
    }
}