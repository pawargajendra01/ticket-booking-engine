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

            const imageHtml = event.imageUrl
                ? `<img src="${event.imageUrl}" alt="${event.name}" style="width:100%; height:160px; object-fit:cover; border-radius:8px; margin-bottom:12px;" onerror="this.style.display='none'">`
                : "";

            card.innerHTML = `
    <div class="card-top">
        ${imageHtml}
        <h2>${event.name}</h2>
        <p style="color:var(--magenta); font-weight:600;">${event.venue || "Venue TBA"}</p>
        <p>${event.eventDateTime || "Date TBA"}</p>
    </div>
    <div class="ticket-divider"></div>
    <div class="card-bottom">
        <p>${event.availableSeats} of ${event.totalSeats} seats available</p>
        <a class="btn ${soldOut ? "btn-secondary" : "btn-primary"}" href="book.html?eventId=${event.id}">
            ${soldOut ? "Join Waitlist" : "Book Now"}
        </a>
    </div>
`;

            eventsList.appendChild(card);
        });
    } catch (error) {
        statusMessage.textContent = `Failed to load events: ${error.message}`;
    }
}

loadEvents();