async function handleWaitlistSubmit(event) {
    event.preventDefault();
    const eventId = document.getElementById("eventId").value;
    const resultsDiv = document.getElementById("waitlist-results");
    const statusMessage = document.getElementById("status-message");

    try {
        statusMessage.textContent = "Loading waitlist...";
        const waitlist = await getWaitlist(eventId);
        statusMessage.textContent = "";

        if (waitlist.length === 0) {
            resultsDiv.innerHTML = "<p>No one is currently waitlisted for this event.</p>";
            return;
        }

        const rows = waitlist.map((entry, index) => `
            <div class="result-box" style="margin-bottom: 12px;">
                <p><strong>Position ${index + 1}</strong></p>
                <p>Booking ID: ${entry.bookingId}</p>
                <p>User: ${entry.userId}</p>
                <p>Priority Tier: ${entry.priorityTier}</p>
            </div>
        `).join("");

        resultsDiv.innerHTML = rows;
    } catch (error) {
        statusMessage.textContent = `Failed to load waitlist: ${error.message}`;
        resultsDiv.innerHTML = "";
    }
}

async function handleCancelSubmit(event) {
    event.preventDefault();
    const bookingId = document.getElementById("bookingId").value;
    const cancelResult = document.getElementById("cancel-result");

    try {
        cancelResult.innerHTML = "<p>Cancelling...</p>";
        const response = await cancelBooking(bookingId);
        cancelResult.innerHTML = `
            <div class="result-box">
                <span class="badge badge-waitlisted">${response.status}</span>
                <p style="margin-top: 12px;">Booking ${response.bookingId} cancelled.</p>
                <p>Seats now remaining: ${response.remainingSeats}</p>
            </div>
        `;
    } catch (error) {
        cancelResult.innerHTML = `<p style="color: red;">Cancellation failed: ${error.message}</p>`;
    }
}

document.getElementById("waitlist-form").addEventListener("submit", handleWaitlistSubmit);
document.getElementById("cancel-form").addEventListener("submit", handleCancelSubmit);