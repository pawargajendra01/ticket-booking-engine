function getEventIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get("eventId");
}

async function loadEventTitle(eventId) {
    try {
        const event = await getEvent(eventId);
        document.getElementById("event-title").textContent = `Book: ${event.name}`;
    } catch (error) {
        document.getElementById("status-message").textContent = `Could not load event: ${error.message}`;
    }
}

function renderResult(response) {
    const resultDiv = document.getElementById("result");
    const badgeClass = response.status === "CONFIRMED" ? "badge-confirmed" : "badge-waitlisted";

    resultDiv.innerHTML = `
        <div class="result-box">
            <span class="badge ${badgeClass}">${response.status}</span>
            <p style="margin-top: 12px;">Booking ID: ${response.bookingId}</p>
            <p>User: ${response.userId}</p>
            <p>Seats remaining: ${response.remainingSeats}</p>
        </div>
    `;
}

async function handleBookingSubmit(event) {
    event.preventDefault();

    const eventId = getEventIdFromUrl();
    const userId = document.getElementById("userId").value;
    const priorityTier = Number(document.getElementById("priorityTier").value);
    const statusMessage = document.getElementById("status-message");

    try {
        statusMessage.textContent = "Submitting booking...";
        const response = await bookSeat(eventId, userId, priorityTier);
        statusMessage.textContent = "";
        renderResult(response);
    } catch (error) {
        statusMessage.textContent = `Booking failed: ${error.message}`;
    }
}

const eventId = getEventIdFromUrl();
if (eventId) {
    loadEventTitle(eventId);
} else {
    document.getElementById("status-message").textContent = "No event selected.";
}

document.getElementById("booking-form").addEventListener("submit", handleBookingSubmit);