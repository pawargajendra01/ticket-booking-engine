const API_BASE_URL = "http://localhost:8080/api";

async function apiRequest(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
        headers: { "Content-Type": "application/json" },
        ...options,
    });

    if (!response.ok) {
        const errorBody = await response.json().catch(() => ({ error: "Unknown error" }));
        throw new Error(errorBody.error || `Request failed with status ${response.status}`);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function getEvents() {
    return apiRequest("/events");
}

function getEvent(eventId) {
    return apiRequest(`/events/${eventId}`);
}

function createEvent(name, totalSeats) {
    return apiRequest("/events", {
        method: "POST",
        body: JSON.stringify({ name, totalSeats }),
    });
}

function bookSeat(eventId, userId, priorityTier) {
    return apiRequest(`/events/${eventId}/book`, {
        method: "POST",
        body: JSON.stringify({ userId, priorityTier }),
    });
}

function cancelBooking(bookingId) {
    return apiRequest(`/bookings/${bookingId}/cancel`, {
        method: "POST",
    });
}

function getWaitlist(eventId) {
    return apiRequest(`/events/${eventId}/waitlist`);
}