package com.shopit.productservice.exception;

public class ProductNotSavedException extends RuntimeException{
    public ProductNotSavedException(String message){
        super(message);
    }
}