let countdownIntervalId = null;

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

function stopCountdown() {
    if (countdownIntervalId) {
        clearInterval(countdownIntervalId);
        countdownIntervalId = null;
    }
}

function startCountdown(deadlineIso) {
    stopCountdown();
    const deadline = new Date(deadlineIso).getTime();
    const timerEl = document.getElementById("countdown-timer");

    function tick() {
        const now = Date.now();
        const remainingMs = deadline - now;

        if (remainingMs <= 0) {
            if (timerEl) {
                timerEl.textContent = "Time expired \u2014 seat may be released";
                timerEl.style.color = "#FF5C7A";
            }
            const retryBtn = document.getElementById("retry-payment-btn");
            if (retryBtn) {
                retryBtn.disabled = true;
                retryBtn.textContent = "Payment Window Expired";
            }
            stopCountdown();
            return;
        }

        const totalSeconds = Math.floor(remainingMs / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        const formatted = `${minutes}:${seconds.toString().padStart(2, "0")}`;

        if (timerEl) {
            timerEl.textContent = `Complete payment within: ${formatted}`;
            timerEl.style.color = remainingMs < 60000 ? "#FF5C7A" : "#F2B705";
        }
    }

    tick();
    countdownIntervalId = setInterval(tick, 1000);
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
            ${response.status === "CONFIRMED" ? '<p id="countdown-timer" style="font-weight: 700; margin-top: 12px;"></p>' : ""}
        </div>
    `;

    if (response.status === "CONFIRMED" && response.paymentDeadline) {
        startCountdown(response.paymentDeadline);
    }
}

function renderPaidResult(bookingId) {
    stopCountdown();
    const resultDiv = document.getElementById("result");
    resultDiv.innerHTML = `
        <div class="result-box">
            <span class="badge badge-confirmed">PAID</span>
            <p style="margin-top: 12px;">Booking ID: ${bookingId}</p>
            <p>Your seat is fully confirmed and paid.</p>
        </div>
    `;
}

function renderPaymentPendingResult(bookingResponse, statusMessage) {
    const resultDiv = document.getElementById("result");
    resultDiv.innerHTML = `
        <div class="result-box">
            <span class="badge badge-waitlisted">PAYMENT PENDING</span>
            <p style="margin-top: 12px;">Booking ID: ${bookingResponse.bookingId}</p>
            <p>${statusMessage}</p>
            <p id="countdown-timer" style="font-weight: 700; margin-top: 12px;"></p>
            <button id="retry-payment-btn" class="btn btn-primary" style="margin-top: 14px;">Retry Payment</button>
        </div>
    `;

    if (bookingResponse.paymentDeadline) {
        startCountdown(bookingResponse.paymentDeadline);
    }

    document.getElementById("retry-payment-btn").addEventListener("click", () => startPayment(bookingResponse));
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
                color: "#F2B705",
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
                    renderPaymentPendingResult(bookingResponse, `Payment verification failed: ${verifyError.message}`);
                }
            },
            modal: {
                ondismiss: function () {
                    renderPaymentPendingResult(bookingResponse, "Payment window closed before completing.");
                },
            },
        };

        const razorpayCheckout = new Razorpay(options);
        razorpayCheckout.open();
    } catch (error) {
        renderPaymentPendingResult(bookingResponse, `Could not start payment: ${error.message}`);
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