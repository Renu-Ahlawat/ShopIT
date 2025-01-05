package com.shopit.orderservice.exception;

import com.shopit.orderservice.constants.OrderConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OrderControllerAdvice {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> exception(OrderNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InventoryNotReachableException.class)
    public ResponseEntity<String> exception(InventoryNotReachableException ex){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotInStockException.class)
    public ResponseEntity<String> exception(ProductNotInStockException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> exception(RateLimitExceededException ex){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    @ExceptionHandler(OrderNotSavedException.class)
    public ResponseEntity<String> exception(OrderNotSavedException ex){
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(ex.getMessage());
    }
}