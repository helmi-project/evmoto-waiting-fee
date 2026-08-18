package com.evmoto.fee.exception;

public class InvalidFeeRequestException extends RuntimeException {
    public InvalidFeeRequestException(String message) {
        super(message);
    }
}
