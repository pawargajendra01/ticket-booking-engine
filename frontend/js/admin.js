async function handleCreateEventSubmit(event) {
    event.preventDefault();

    const name = document.getElementById("eventName").value;
    const totalSeats = Number(document.getElementById("totalSeats").value);
    const statusMessage = document.getElementById("status-message");
    const resultDiv = document.getElementById("result");

    try {
        statusMessage.textContent = "Creating event...";
        const newEvent = await createEvent(name, totalSeats);
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
    } catch (error) {
        statusMessage.textContent = `Failed to create event: ${error.message}`;
    }
}

document.getElementById("create-event-form").addEventListener("submit", handleCreateEventSubmit);