package com.shopit.inventoryservice.exception;

public class InventoryNotSavedException extends RuntimeException{
    public InventoryNotSavedException(String message){
        super(message);
    }
}