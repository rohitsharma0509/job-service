package com.scb.job.exception;

public class InvalidJobStateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidJobStateException(String message) {
        super(message);
    }

}
