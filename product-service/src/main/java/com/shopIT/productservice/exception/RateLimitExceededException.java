package com.shopit.productservice.exception;

public class RateLimitExceededException extends RuntimeException{
    public RateLimitExceededException(String message){
        super(message);
    }
}