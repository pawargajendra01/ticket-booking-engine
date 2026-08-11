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

function renderPaidResult(bookingId) {
    const resultDiv = document.getElementById("result");
    resultDiv.innerHTML = `
        <div class="result-box">
            <span class="badge badge-confirmed">PAID</span>
            <p style="margin-top: 12px;">Booking ID: ${bookingId}</p>
            <p>Your seat is fully confirmed and paid.</p>
        </div>
    `;
}

function renderPaymentPendingResult(bookingId, statusMessage) {
    const resultDiv = document.getElementById("result");
    resultDiv.innerHTML = `
        <div class="result-box">
            <span class="badge badge-waitlisted">PAYMENT PENDING</span>
            <p style="margin-top: 12px;">Booking ID: ${bookingId}</p>
            <p>${statusMessage}</p>
            <p>Your seat is held for 10 minutes. Pay before it expires or it will be released.</p>
        </div>
    `;
}

async function startPayment(bookingResponse) {
    const bookingId = bookingResponse.bookingId;

    try {
        const order = await createPaymentOrder(bookingId);

        const options = {
            key: order.keyId,
            amount: order.amount,
            currency: order.currency,
            name: "TicketHub",
            description: "Event seat payment",
            order_id: order.orderId,
            prefill: {
                name: bookingResponse.userId,
            },
            theme: {
                color: "#4f46e5",
            },
            handler: async function (razorpayResponse) {
                try {
                    await verifyPayment(
                        bookingId,
                        razorpayResponse.razorpay_order_id,
                        razorpayResponse.razorpay_payment_id,
                        razorpayResponse.razorpay_signature
                    );
                    renderPaidResult(bookingId);
                } catch (verifyError) {
                    renderPaymentPendingResult(bookingId, `Payment verification failed: ${verifyError.message}`);
                }
            },
            modal: {
                ondismiss: function () {
                    renderPaymentPendingResult(bookingId, "Payment window closed before completing.");
                },
            },
        };

        const razorpayCheckout = new Razorpay(options);
        razorpayCheckout.open();
    } catch (error) {
        renderPaymentPendingResult(bookingId, `Could not start payment: ${error.message}`);
    }
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

        if (response.status === "CONFIRMED") {
            await startPayment(response);
        }
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