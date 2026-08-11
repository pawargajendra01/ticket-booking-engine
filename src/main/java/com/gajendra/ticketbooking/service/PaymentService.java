package com.gajendra.ticketbooking.service;

import com.gajendra.ticketbooking.dto.PaymentOrderResponse;
import com.gajendra.ticketbooking.entity.Booking;
import com.gajendra.ticketbooking.entity.BookingStatus;
import com.gajendra.ticketbooking.exception.BookingNotFoundException;
import com.gajendra.ticketbooking.exception.InvalidBookingStateException;
import com.gajendra.ticketbooking.repository.BookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final long TICKET_PRICE_PAISE = 50000; // flat ₹500 per seat (demo pricing)

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final BookingRepository bookingRepository;

    public PaymentService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public PaymentOrderResponse createOrderForBooking(Long bookingId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException("Booking " + bookingId + " is not in a payable state");
        }
        if (booking.isPaid()) {
            throw new InvalidBookingStateException("Booking " + bookingId + " is already paid");
        }

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", TICKET_PRICE_PAISE);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "booking_" + bookingId);

        Order order = client.orders.create(orderRequest);
        String orderId = order.get("id");

        return new PaymentOrderResponse(orderId, TICKET_PRICE_PAISE, "INR", keyId, bookingId);
    }

    @Transactional
    public void verifyAndMarkPaid(Long bookingId, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws Exception {
        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", razorpayOrderId);
        attributes.put("razorpay_payment_id", razorpayPaymentId);
        attributes.put("razorpay_signature", razorpaySignature);

        boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);
        if (!isValid) {
            throw new InvalidBookingStateException("Payment signature verification failed for booking " + bookingId);
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        booking.setPaid(true);
        bookingRepository.save(booking);
    }
}