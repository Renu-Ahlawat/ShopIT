package com.shopit.inventoryservice.controller;

import com.shopit.inventoryservice.config.RateLimitConfig;
import com.shopit.inventoryservice.constants.InventoryConstants;
import com.shopit.inventoryservice.dto.InventoryDtoRequest;
import com.shopit.inventoryservice.dto.InventoryDtoResponse;
import com.shopit.inventoryservice.exception.RateLimitExceededException;
import com.shopit.inventoryservice.service.InventoryService;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    private final RateLimitConfig rateLimitConfig;

    public InventoryController(InventoryService inventoryService, RateLimitConfig rateLimitConfig) {
        this.inventoryService = inventoryService;
        this.rateLimitConfig = rateLimitConfig;
    }

    @Value("${api.username}")
    private String username;

    // Using @RequestParam for multiple inputs
    @GetMapping("/quantity")
    public ResponseEntity<List<InventoryDtoResponse>> quantityInStock(@RequestParam List<String> skuCode) {
        Bucket bucket = rateLimitConfig.resolveBucket(username); // Getting the bucket

        if (bucket.tryConsume(1)) {
            log.info(String.format(InventoryConstants.API_CHECK, bucket.getAvailableTokens() + 1,
                    "quantityInStock allowed"));
            final List<InventoryDtoResponse> inventoryDtoResponseList = inventoryService.quantityInStock(skuCode);
            return ResponseEntity.status(HttpStatus.OK).body(inventoryDtoResponseList);
        } else {
            log.info(String.format(InventoryConstants.API_CHECK, "0", "quantityInStock rejected"));
            throw new RateLimitExceededException(InventoryConstants.RATE_LIMIT_EXCEEDED);
        }
    }

    @PostMapping("/addInInventory")
    public ResponseEntity<String> addInInventory(@RequestBody InventoryDtoRequest inventoryDtoRequest) {
        Bucket bucket = rateLimitConfig.resolveBucket(username); // Getting the bucket

        if (bucket.tryConsume(1)) {
            log.info(String.format(InventoryConstants.API_CHECK, bucket.getAvailableTokens() + 1,
                    "addInInventory allowed"));
            final Integer id = inventoryService.addInInventory(inventoryDtoRequest);
            return ResponseEntity.status(HttpStatus.OK).body(InventoryConstants.ADDED_IN_INVENTORY + id);
        } else {
            log.info(String.format(InventoryConstants.API_CHECK, "0", "addInInventory rejected"));
            throw new RateLimitExceededException(InventoryConstants.RATE_LIMIT_EXCEEDED);
        }
    }
}