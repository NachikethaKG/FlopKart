package com.flopkart.order.exception;

public class StockReservationException extends RuntimeException {
    private final int statusCode;

    public StockReservationException(String message) {
        super(message);
        this.statusCode = 409;
    }

    public StockReservationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
