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
    ${imageHtml}
    <h2>${event.name}</h2>
    <p style="color:#4f46e5; font-weight:600; font-size:13px; margin-bottom:4px;">${event.venue || "Venue TBA"}</p>
    <p style="color:#888; font-size:13px; margin-bottom:8px;">${event.eventDateTime || "Date TBA"}</p>
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