async function handleCreateEventSubmit(event) {
    event.preventDefault();

    const name = document.getElementById("eventName").value;
    const venue = document.getElementById("venue").value;
    const eventDateTime = document.getElementById("eventDateTime").value;
    const totalSeats = Number(document.getElementById("totalSeats").value);
    const description = document.getElementById("description").value;
    const imageUrl = document.getElementById("imageUrl").value;
    const statusMessage = document.getElementById("status-message");
    const resultDiv = document.getElementById("result");

    try {
        statusMessage.textContent = "Creating event...";
        const newEvent = await createEvent(name, totalSeats, venue, eventDateTime, description, imageUrl);
        statusMessage.textContent = "";

        resultDiv.innerHTML = `
            <div class="result-box">
                <span class="badge badge-confirmed">CREATED</span>
                <p style="margin-top: 12px;">Event ID: ${newEvent.id}</p>
                <p>${newEvent.name} — ${newEvent.totalSeats} seats</p>
                <a href="index.html" class="btn btn-primary" style="margin-top: 12px; display: inline-block;">View on Events Page</a>
            </div>
        `;

        document.getElementById("create-event-form").reset();
        loadExistingEvents();
    } catch (error) {
        statusMessage.textContent = `Failed to create event: ${error.message}`;
    }
}

async function handleDeleteClick(eventId, buttonEl) {
    const confirmed = confirm(`Delete event #${eventId}? This cannot be undone.`);
    if (!confirmed) {
        return;
    }

    try {
        buttonEl.disabled = true;
        buttonEl.textContent = "Deleting...";
        await deleteEvent(eventId);
        loadExistingEvents();
    } catch (error) {
        if (error.message.includes("active bookings")) {
            const forceConfirmed = confirm(
                `WARNING: Event #${eventId} has customers with confirmed or waitlisted bookings.\n\nForce-deleting will PERMANENTLY CANCEL all of their bookings. This cannot be undone.\n\nAre you absolutely sure?`
            );

            if (forceConfirmed) {
                try {
                    await deleteEvent(eventId, true);
                    loadExistingEvents();
                    return;
                } catch (forceError) {
                    alert(`Could not delete event: ${forceError.message}`);
                }
            }
        } else {
            alert(`Could not delete event: ${error.message}`);
        }

        buttonEl.disabled = false;
        buttonEl.textContent = "Delete";
    }
}

async function loadExistingEvents() {
    const listDiv = document.getElementById("existing-events-list");
    listDiv.innerHTML = "<p>Loading...</p>";

    try {
        const events = await getEvents();

        if (events.length === 0) {
            listDiv.innerHTML = "<p>No events created yet.</p>";
            return;
        }

        listDiv.innerHTML = "";
        events.forEach((event) => {
            const row = document.createElement("div");
            row.className = "event-card";
            row.style.marginBottom = "12px";

            const deleteBtn = document.createElement("button");
            deleteBtn.className = "btn btn-secondary";
            deleteBtn.textContent = "Delete";
            deleteBtn.addEventListener("click", () => handleDeleteClick(event.id, deleteBtn));

            const info = document.createElement("div");
            info.className = "card-top";
            info.innerHTML = `
    <h2>${event.name} (ID: ${event.id})</h2>
    <p>${event.venue || "No venue set"} \u2022 ${event.eventDateTime || "No date set"}</p>
    <p>${event.availableSeats} / ${event.totalSeats} seats available</p>
`;

            const divider = document.createElement("div");
            divider.className = "ticket-divider";

            const bottom = document.createElement("div");
            bottom.className = "card-bottom";
            bottom.appendChild(deleteBtn);

            row.appendChild(info);
            row.appendChild(divider);
            row.appendChild(bottom);
            listDiv.appendChild(row);
        });
    } catch (error) {
        listDiv.innerHTML = `<p>Failed to load events: ${error.message}</p>`;
    }
}

document.getElementById("create-event-form").addEventListener("submit", handleCreateEventSubmit);
loadExistingEvents();