package com.shopit.productservice.exception;

import com.shopit.productservice.constants.ProductConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice // used to handle the exceptions globally.
public class ProductControllerAdvice {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> exception(ProductNotFoundException ex){  // Here whatever the return type we are stating, it will override the return type which is originally mentioned in the Controller class's API.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotSavedException.class)
    public ResponseEntity<String> exception(ProductNotSavedException ex){  // Here whatever the return type we are stating, it will override the return type which is originally mentioned in the Controller class's API.
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> exception(RateLimitExceededException ex){  // Here whatever the return type we are stating, it will override the return type which is originally mentioned in the Controller class's API.
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }
}