# TicketHub — Real-Time Concert Ticket Booking Platform

A full-stack ticket booking platform: a Spring Boot backend that allocates
limited event seats to concurrent requests with ACID-safe transactions and
a custom priority-queue waitlist, a vanilla JS frontend for browsing and
booking events, and real Razorpay payment integration with a time-limited
seat hold that auto-releases if payment isn't completed.

Live demo: https://tickethub-pq.netlify.app (backend hosted on Railway, may briefly cold-start on first load)

Built to demonstrate: Java OOP, Spring Boot REST APIs, SQL ACID
transactions, a hand-written data structure (binary min-heap) used for a
real performance reason, scheduled background jobs, and payment gateway
integration with server-side signature verification.

## Tech Stack
- **Backend:** Java 17, Spring Boot 4.1, Spring Data JPA + Hibernate, MySQL (InnoDB)
- **Frontend:** HTML5, CSS3, vanilla JavaScript (Fetch API) — no framework
- **Payments:** Razorpay (test mode), server-side signature verification
- **Testing:** JUnit 5 — unit tests + a concurrency stress test

## Architecture
```
frontend/
index.html, book.html, my-bookings.html, admin.html
js/api.js shared fetch helper for all pages
css/styles.css

src/main/java/com/gajendra/ticketbooking/
controller/ REST endpoints (thin — no business logic)
service/ BookingService (transactions), WaitlistManager (heap owner),
PaymentService (Razorpay), PaymentExpiryScheduler
datastructure/CustomPriorityQueue (binary min-heap), WaitlistRequest
entity/ Event, Booking (JPA entities)
repository/ Spring Data repositories, incl. pessimistic-lock query
dto/ Request/response shapes, decoupled from entities
exception/ Custom exceptions + a @RestControllerAdvice handler
config/ CORS configuration
```

## How booking + payment fits together

1. `POST /api/events/{id}/book` — reserves a seat if available (`CONFIRMED`)
   or joins the waitlist (`WAITLISTED`). A `CONFIRMED` booking gets a
   **10-minute payment deadline**.
2. The frontend immediately opens Razorpay Checkout for `CONFIRMED` bookings.
3. On payment success, the frontend sends the payment ID, order ID, and
   signature to `POST /api/bookings/{id}/verify-payment`, which
   cryptographically verifies the signature server-side using
   `Utils.verifyPaymentSignature` before marking the booking `paid`.
   **The frontend's "payment succeeded" callback is never trusted alone.**
4. A `@Scheduled` job (`PaymentExpiryScheduler`) runs every 30 seconds,
   finds `CONFIRMED` bookings that are unpaid and past their deadline, and
   cancels them automatically — reusing the exact same `cancelBooking()`
   method a manual cancellation uses, which frees the seat and
   auto-promotes the next waitlisted customer via the priority queue.

## Why a custom PriorityQueue instead of `java.util.PriorityQueue`

The waitlist needs to repeatedly answer "who's next?" ordered by priority
tier and then request time. A naive `List`-based waitlist would need an
O(n) scan every time a seat frees up. `CustomPriorityQueue` is a
hand-rolled, array-backed binary min-heap giving O(log n) insert and
O(log n) extract-min instead.

`WaitlistManager` keeps one heap per event so waitlist operations on
different events never contend with each other, and synchronizes access
to each individual heap since the heap itself is not thread-safe.

## How ACID is enforced

`BookingService.bookSeat()` and `cancelBooking()` are both
`@Transactional`. `EventRepository.findByIdForUpdate()` issues
`SELECT ... FOR UPDATE`, taking a row-level lock on the event so
concurrent requests for the same event are serialized at the database
level. This is proven by `ConcurrencyBookingTest`, which fires 25
concurrent booking requests at a 1-seat event and asserts exactly 1 is
`CONFIRMED`.

## Running it locally

**Backend:**
1. Create the database: `CREATE DATABASE ticketdb;`
2. Set environment variables (never committed): `DB_PASSWORD`,
   `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`
3. Run: `.\mvnw spring-boot:run`

**Frontend:**
1. Install the "Live Server" VS Code extension
2. Right-click `frontend/index.html` → "Open with Live Server"

## API Reference

| Method | Endpoint                              | Body                                        |
|--------|-----------------------------------------|----------------------------------------------|
| GET    | `/api/events`                         | —                                              |
| POST   | `/api/events`                         | `{"name": "...", "totalSeats": 50}`            |
| GET    | `/api/events/{id}`                    | —                                              |
| POST   | `/api/events/{id}/book`               | `{"userId": "...", "priorityTier": 1}`         |
| POST   | `/api/bookings/{id}/cancel`           | —                                              |
| GET    | `/api/events/{id}/waitlist`           | —                                              |
| POST   | `/api/bookings/{id}/payment-order`    | —                                              |
| POST   | `/api/bookings/{id}/verify-payment`   | `{"razorpayOrderId": "...", "razorpayPaymentId": "...", "razorpaySignature": "..."}` |

## Running the tests
```
.\mvnw test
```

- `CustomPriorityQueueTest` — verifies the heap's sort order, including a
  1000-element randomized stress test.
- `ConcurrencyBookingTest` — fires 25 concurrent booking requests at a
  1-seat event, asserting exactly one is confirmed.

## Possible extensions
- Dynamic per-event ticket pricing (currently a flat demo price)
- Idempotency keys on `bookSeat` for safe client retries
- Redis-backed distributed lock if horizontally scaled across instances