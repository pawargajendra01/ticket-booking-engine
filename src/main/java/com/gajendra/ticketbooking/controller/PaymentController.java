package com.gajendra.ticketbooking.controller;

import com.gajendra.ticketbooking.dto.PaymentOrderResponse;
import com.gajendra.ticketbooking.dto.VerifyPaymentRequest;
import com.gajendra.ticketbooking.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{bookingId}/payment-order")
    public PaymentOrderResponse createPaymentOrder(@PathVariable Long bookingId) throws Exception {
        return paymentService.createOrderForBooking(bookingId);
    }

    @PostMapping("/{bookingId}/verify-payment")
    public Map<String, Object> verifyPayment(@PathVariable Long bookingId, @Valid @RequestBody VerifyPaymentRequest request) throws Exception {
        paymentService.verifyAndMarkPaid(
                bookingId,
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );
        return Map.of("bookingId", bookingId, "paid", true);
    }
}