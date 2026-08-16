package org.example.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Too many requests, please slow down and try again shortly");
    }
}
