package com.shopit.inventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.inventoryservice.config.RateLimitConfig;
import com.shopit.inventoryservice.constants.InventoryConstants;
import com.shopit.inventoryservice.dto.InventoryDtoRequest;
import com.shopit.inventoryservice.dto.InventoryDtoResponse;
import com.shopit.inventoryservice.service.InventoryService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @InjectMocks
    private InventoryController inventoryController;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private RateLimitConfig rateLimitConfig;

    private MockMvc mockMvc;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void mockMvcSetup() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();

    }

    @Test
    void quantityInStockTestValid() throws Exception {
        InventoryDtoResponse inventoryDtoResponse = InventoryDtoResponse.builder()
                .id(1)
                .quantity(101)
                .skuCode("OnePlus 11R")
                .build();

        // Set the value using reflection
        ReflectionTestUtils.setField(inventoryController, "username", "gaurav");

        Bucket bucket = Bucket4j.builder().addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)))).build();

        Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);
        Mockito.when(inventoryService.quantityInStock(any(List.class))).thenReturn(List.of(inventoryDtoResponse));

        ResultActions resultAction = mockMvc.perform(get("/inventory/quantity?skuCode='OnePlus 11R'")
                .contentType(MediaType.APPLICATION_JSON)
        );

        resultAction
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(inventoryDtoResponse))));
    }

    @Test
    void quantityInStockTestInvalid() throws Exception {
        // Set the value using reflection
        ReflectionTestUtils.setField(inventoryController, "username", "gaurav");

        Bucket bucket = Bucket4j.builder().addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1)))).build();
        bucket.tryConsume(1);

        Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);

        Assertions.assertThrows(ServletException.class, () -> mockMvc.perform(get("/inventory/quantity?skuCode='OnePlus 11R'")
                .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    void addInInventoryTestValid() throws Exception {
        InventoryDtoRequest inventoryDtoRequest = InventoryDtoRequest.builder()
                .quantity(101)
                .skuCode("OnePlus 11R")
                .build();

        // Set the value using reflection
        ReflectionTestUtils.setField(inventoryController, "username", "gaurav");

        Bucket bucket = Bucket4j.builder().addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)))).build();

        Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);
        Mockito.when(inventoryService.addInInventory(any(InventoryDtoRequest.class))).thenReturn(1);

        ResultActions resultAction = mockMvc.perform(post("/inventory/addInInventory")
                .content(objectMapper.writeValueAsString(inventoryDtoRequest))
                .contentType(MediaType.APPLICATION_JSON));

        resultAction
                .andExpect(status().isOk())
                .andExpect(content().string(InventoryConstants.ADDED_IN_INVENTORY + 1));
    }

    @Test
    void addInInventoryTestInvalid() throws Exception {
        InventoryDtoRequest inventoryDtoRequest = InventoryDtoRequest.builder()
                .quantity(101)
                .skuCode("OnePlus 11R")
                .build();

        // Set the value using reflection
        ReflectionTestUtils.setField(inventoryController, "username", "gaurav");

        Bucket bucket = Bucket4j.builder().addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1)))).build();
        bucket.tryConsume(1);

        Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);

        Assertions.assertThrows(ServletException.class, () -> mockMvc.perform(post("/inventory/addInInventory")
                .content(objectMapper.writeValueAsString(inventoryDtoRequest))
                .contentType(MediaType.APPLICATION_JSON))
        );
    }
}