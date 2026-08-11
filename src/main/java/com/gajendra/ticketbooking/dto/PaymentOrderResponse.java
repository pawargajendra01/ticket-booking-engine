package com.gajendra.ticketbooking.dto;

public class PaymentOrderResponse {

    private final String orderId;
    private final long amount;
    private final String currency;
    private final String keyId;
    private final Long bookingId;

    public PaymentOrderResponse(String orderId, long amount, String currency, String keyId, Long bookingId) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
        this.bookingId = bookingId;
    }

    public String getOrderId() { return orderId; }
    public long getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getKeyId() { return keyId; }
    public Long getBookingId() { return bookingId; }
}