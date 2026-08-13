package com.gajendra.ticketbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int availableSeats;
    private String venue;
    private String eventDateTime;
    private String description;
    private String imageUrl;

    protected Event() {
        // JPA needs an empty constructor - don't call this yourself
    }

    public Event(String name, int totalSeats) {
        this.name = name;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    public Event(String name, int totalSeats, String venue, String eventDateTime, String description, String imageUrl) {
        this.name = name;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.venue = venue;
        this.eventDateTime = eventDateTime;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public String getVenue() {
        return venue;
    }

    public String getEventDateTime() {
        return eventDateTime;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    @Version
    private Long version;

    public Long getVersion() {
        return version;
    }

    public boolean hasSeatAvailable() {
        return availableSeats > 0;
    }

    public void decrementSeat() {
        if (availableSeats <= 0) {
            throw new IllegalStateException("No seats available to decrement for event id=" + id);
        }
        availableSeats--;
    }

    public void incrementSeat() {
        if (availableSeats >= totalSeats) {
            throw new IllegalStateException("Cannot exceed totalSeats for event id=" + id);
        }
        availableSeats++;
    }
}
