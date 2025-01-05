package com.shopit.productservice.controller;

import com.shopit.productservice.constants.ProductConstants;
import com.shopit.productservice.config.RateLimitConfig;
import com.shopit.productservice.dto.ProductDtoRequest;
import com.shopit.productservice.dto.ProductDtoResponse;
import com.shopit.productservice.exception.RateLimitExceededException;
import com.shopit.productservice.service.ProductService;

import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController {

    private final ProductService prodService;

    private final RateLimitConfig rateLimitConfig;

    @Autowired
    public ProductController(ProductService prodService, RateLimitConfig rateLimitConfig) {
        this.prodService = prodService;
        this.rateLimitConfig = rateLimitConfig;
    }

    // Either you can use this way using Slf4j library, or Lombok already provides
    // @Slf4j annotation to use directly.
    // private static final Logger logger =
    // LoggerFactory.getLogger(ProductController.class);

    @Value("${api.username}")
    private String username;

    @PostMapping("/addProduct")
    public ResponseEntity<String> addProduct(@RequestBody ProductDtoRequest productDtoRequest) {
        Bucket bucket = rateLimitConfig.resolveBucket(username);

        if (bucket.tryConsume(1)) {
            log.info(String.format(ProductConstants.API_CHECK, bucket.getAvailableTokens() + 1, "AddProduct allowed"));
            final Integer productId = prodService.addProduct(productDtoRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(ProductConstants.ADDED_PRODUCT + productId);
        } else {
            log.info(String.format(ProductConstants.API_CHECK, "0", "AddProduct rejected"));
            throw new RateLimitExceededException(ProductConstants.RATE_LIMIT_EXCEEDED);
        }
    }

    // ------------------------- API VERSIONING
    // -------------------------------------
    // ------------------------------------------------------------------------------
    // 4 Ways:

    // 1. URI Versioning:
    // @GetMapping("/v1/getAllProducts") and @GetMapping("/v2/getAllProducts")

    // 2. Request Parameter Versioning:
    // @GetMapping(value = "/getAllProducts", params = "version=1") and
    // @GetMapping(value = "/getAllProducts", params = "version=2")
    // e.g. postman call: http://localhost:8080/getAllProducts?version=1 and
    // http://localhost:8080/getAllProducts?version=2

    // 3. (Custom) Headers versioning:
    // @GetMapping(value = "/getAllProducts", headers = "X-API-VERSION=1") and
    // @GetMapping(value = "/getAllProducts", headers = "X-API-VERSION=2")
    // e.g. postman call: http://localhost:8080/getAllProducts with header key:
    // X-API-VERSION and value: 1 and http://localhost:8080/getAllProducts with
    // header key: X-API-VERSION and value: 2

    // 4. Media type OR Content Negotiation OR Accept Header Versioning:
    // @GetMapping(value = "/getAllProducts", produces = "application/v1+json") and
    // @GetMapping(value = "/getAllProducts", produces = "application/v2+json")
    // e.g. postman call: http://localhost:8080/getAllProducts using standard header
    // key: ACCEPT and value: application/v1+json and
    // http://localhost:8080/getAllProducts using standard header key: ACCEPT and
    // value: application/v2+json

    // NOTE: Every API versioning ways have some drawbacks so don't use unless
    // necessary.

    // API VERSIONING using HEADERS VERSIONING:
    @GetMapping(value = "/getAllProducts", headers = "X-API-VERSION=1")
    public ResponseEntity<List<ProductDtoResponse>> getAllProductsV1() {
        Bucket bucket = rateLimitConfig.resolveBucket(username);

        if (bucket.tryConsume(1)) {
            log.info(String.format(ProductConstants.API_CHECK, bucket.getAvailableTokens() + 1,
                    "getAllProducts allowed"));
            final List<ProductDtoResponse> productDtoResponse = prodService.getAllProducts();
            return ResponseEntity.status(HttpStatus.OK).body(productDtoResponse);
        } else {
            log.info(String.format(ProductConstants.API_CHECK, "0", "getAllProducts rejected"));
            throw new RateLimitExceededException(ProductConstants.RATE_LIMIT_EXCEEDED);
        }
    }

    @GetMapping(value = "/getAllProducts", headers = "X-API-VERSION=2")
    public ResponseEntity<Object> getAllProductsV2() {
        Bucket bucket = rateLimitConfig.resolveBucket(username);

        if (bucket.tryConsume(1)) {
            log.info(String.format(ProductConstants.API_CHECK, bucket.getAvailableTokens() + 1,
                    "getAllProducts allowed"));
            final List<ProductDtoResponse> productDtoResponse = prodService.getAllProducts();
            return ResponseEntity.status(HttpStatus.OK).body(productDtoResponse);
        } else {
            log.info(String.format(ProductConstants.API_CHECK, "0", "getAllProducts rejected"));
            throw new RateLimitExceededException(ProductConstants.RATE_LIMIT_EXCEEDED);
        }
    }
}