package com.cinesmart.common.exception;

public class SeatUnavailableException extends RuntimeException {

    private final String errorCode;

    public SeatUnavailableException(String message) {
        super(message);
        this.errorCode = "SEAT_UNAVAILABLE";
    }

    public SeatUnavailableException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
