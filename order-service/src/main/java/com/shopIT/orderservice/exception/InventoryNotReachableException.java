package com.shopit.orderservice.exception;

public class InventoryNotReachableException extends RuntimeException{
    public InventoryNotReachableException(String message){
        super(message);
    }
}