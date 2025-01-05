package com.shopit.orderservice.exception;

public class OrderNotSavedException extends RuntimeException{
    public OrderNotSavedException(String message){
        super(message);
    }
}