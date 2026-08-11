async function loadEvents() {
    const statusMessage = document.getElementById("status-message");
    const eventsList = document.getElementById("events-list");

    try {
        statusMessage.textContent = "Loading events...";
        const events = await getEvents();

        if (events.length === 0) {
            statusMessage.textContent = "No events yet. Check back soon!";
            return;
        }

        statusMessage.textContent = "";
        eventsList.innerHTML = "";

        events.forEach((event) => {
            const card = document.createElement("div");
            card.className = "event-card";

            const soldOut = event.availableSeats === 0;

            card.innerHTML = `
                <h2>${event.name}</h2>
                <p>${event.availableSeats} of ${event.totalSeats} seats available</p>
                <a class="btn ${soldOut ? "btn-secondary" : "btn-primary"}" href="book.html?eventId=${event.id}">
                    ${soldOut ? "Join Waitlist" : "Book Now"}
                </a>
            `;

            eventsList.appendChild(card);
        });
    } catch (error) {
        statusMessage.textContent = `Failed to load events: ${error.message}`;
    }
}

loadEvents();