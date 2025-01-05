package com.shopit.orderservice.controller;

import com.shopit.orderservice.constants.OrderConstants;
import com.shopit.orderservice.config.RateLimitConfig;
import com.shopit.orderservice.dto.OrderDtoRequest;
import com.shopit.orderservice.dto.OrderDtoResponse;
import com.shopit.orderservice.exception.RateLimitExceededException;
import com.shopit.orderservice.service.OrderService;

import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
//@CrossOrigin(methods = { RequestMethod.GET, RequestMethod.POST }, maxAge = 3600) // methods specifies that only GET and POST methods are allowed for this class. If a request with any other
// method (e.g., PUT, DELETE) is made to this endpoint from a different origin, it will be rejected.
@Slf4j
public class OrderController {

    private final OrderService orderService;

    private final RateLimitConfig rateLimitConfig;

    @Autowired
    public OrderController(OrderService orderService, RateLimitConfig rateLimitConfig) {
        this.orderService = orderService;
        this.rateLimitConfig = rateLimitConfig;
    }

    @Value("${api.username}")
    private String username;

    // @CrossOrigin(origins = "http://localhost:8080", maxAge = 1000) //Allow
    // crossOrigin request from this origin only
    // maxAge In this example, any preflight requests for the endpoints will be
    // cached by the client for 1 hour (3600 seconds), allowing subsequent requests
    // from the same origin to bypass the preflight check.
    // When a web application makes a cross-origin request that may have
    // implications for user data (such as requests that use methods other than GET,
    // POST, or HEAD, or that send custom headers), the browser first sends an
    // OPTIONS request (preflight request) to the server to determine if the actual
    // request is safe to send. This preflight request includes headers such as
    // Origin (indicating the requesting origin) and Access-Control-Request-Method
    // (indicating the HTTP method of the actual request).
    // The server then responds to the preflight request with headers indicating
    // whether the actual request is allowed. If the server responds with the
    // appropriate headers (such as Access-Control-Allow-Origin and
    // Access-Control-Allow-Methods), the browser proceeds to send the actual
    // request. Otherwise, the browser blocks the actual request, preventing
    // potential security risks.
    @GetMapping("/getOrderDetails/{orderId}")
    public ResponseEntity<OrderDtoResponse> getOrderDetails(@PathVariable Integer orderId) {
        Bucket bucket = rateLimitConfig.resolveBucket(username); // Getting the bucket

        if (bucket.tryConsume(1)) {
            log.info(String.format(OrderConstants.API_CHECK, bucket.getAvailableTokens() + 1,
                    "getOrderDetails allowed"));
            final OrderDtoResponse orderDtoResponse = orderService.getOrderDetails(orderId);
            return ResponseEntity.status(HttpStatus.OK).body(orderDtoResponse);
        } else {
            log.info(String.format(OrderConstants.API_CHECK, "0", "getOrderDetails rejected"));
            throw new RateLimitExceededException(OrderConstants.RATE_LIMIT_EXCEEDED);
        }
    }

    @PostMapping("/placeOrder")
    public ResponseEntity<String> placeOrder(@RequestBody OrderDtoRequest orderDtoRequest) {
        Bucket bucket = rateLimitConfig.resolveBucket(username);

        if (bucket.tryConsume(1)) {
            log.info(String.format(OrderConstants.API_CHECK, bucket.getAvailableTokens() + 1, "placeOrder allowed"));
            final Integer orderId = orderService.placeOrder(orderDtoRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(OrderConstants.PLACED_ORDER + orderId);
        } else {
            log.info(String.format(OrderConstants.API_CHECK, "0", "placeOrder rejected"));
            throw new RateLimitExceededException(OrderConstants.RATE_LIMIT_EXCEEDED);
        }
    }
}