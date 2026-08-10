# Real-Time Concert Ticket Booking Engine

A backend booking engine that allocates limited event seats to concurrent
requests — confirming bookings when seats are available and routing
overflow demand to a priority-ordered waitlist.

Built to demonstrate: Java OOP, Spring Boot REST APIs, SQL ACID
transactions, and a hand-written data structure (a binary min-heap
priority queue) used for a real performance reason instead of a
textbook exercise.

## Tech Stack
- Java 17, Spring Boot 4.1
- Spring Data JPA + Hibernate
- MySQL (InnoDB)
- JUnit 5 for unit + concurrency testing

## Architecture
```
controller/     REST endpoints (thin, no business logic)
service/        BookingService (transactions), WaitlistManager (heap owner)
datastructure/  CustomPriorityQueue (binary min-heap), WaitlistRequest
entity/         Event, Booking (JPA entities)
repository/     Spring Data repositories, incl. pessimistic-lock query
dto/            Request/response shapes, decoupled from entities
exception/      Custom exceptions plus a RestControllerAdvice handler
```

## Why a custom PriorityQueue instead of `java.util.PriorityQueue`

The waitlist needs to repeatedly answer "who's next?" ordered by
priority tier and then request time. A naive `List`-based waitlist
would need an O(n) scan every time a seat frees up to find the
next-highest-priority customer. `CustomPriorityQueue` is a
hand-rolled, array-backed binary min-heap giving O(log n) insert and
O(log n) extract-min instead.

`WaitlistManager` keeps one heap per event so waitlist operations on
different events never contend with each other, and synchronizes
access to each individual heap since the heap itself is not
thread-safe.

## How ACID is enforced

`BookingService.bookSeat()` and `cancelBooking()` are both
`@Transactional`. The critical section:

1. `EventRepository.findByIdForUpdate()` issues `SELECT ... FOR UPDATE`,
   taking a row-level lock on the event (supported because the table
   uses the InnoDB engine).
2. The seat-availability check, the seat-count decrement, and the
   `Booking` insert all happen inside that same transaction.
3. A second concurrent request for the same event blocks on the lock
   until the first transaction commits or rolls back.

This is proven by `ConcurrencyBookingTest`, which fires 25 concurrent
booking requests at a 1-seat event and asserts exactly 1 is
`CONFIRMED` and the rest are `WAITLISTED`.

## Running it locally

1. Have MySQL running locally, and create the database:
```sql
   CREATE DATABASE ticketdb;
```
2. Set your DB password as an environment variable (never committed):
```powershell
   $env:DB_PASSWORD = "your_mysql_password"
```
3. Run:
```powershell
   .\mvnw spring-boot:run
```

## API Reference

| Method | Endpoint                    | Body                                         |
|--------|------------------------------|-----------------------------------------------|
| POST   | `/api/events`                | `{"name": "Coldplay Live", "totalSeats": 1}`   |
| GET    | `/api/events/{id}`           | —                                               |
| POST   | `/api/events/{id}/book`      | `{"userId": "u1", "priorityTier": 1}`          |
| POST   | `/api/bookings/{id}/cancel`  | —                                               |
| GET    | `/api/events/{id}/waitlist`  | —                                               |

## Running the tests

```powershell
.\mvnw test
```

- `CustomPriorityQueueTest` — verifies the heap extracts elements in
  correct sorted order, including a 1000-element randomized stress test.
- `ConcurrencyBookingTest` — fires 25 concurrent booking requests at a
  1-seat event via a fixed thread pool + `CountDownLatch`, asserting
  exactly one is confirmed.

## Possible extensions
- Idempotency keys on `bookSeat` to make retried client requests safe.
- A scheduled job to expire un-confirmed waitlist offers after N minutes.
- Redis-backed distributed lock if horizontally scaled across multiple
  instances (the current `synchronized` heap access only guards a
  single JVM).