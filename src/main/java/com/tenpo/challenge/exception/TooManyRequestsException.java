package com.tenpo.challenge.exception;

public class TooManyRequestsException extends Exception{
    public TooManyRequestsException(String message) {
        super(message);
    }
}
