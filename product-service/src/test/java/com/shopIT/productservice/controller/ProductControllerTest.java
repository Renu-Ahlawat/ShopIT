package com.shopit.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.productservice.config.RateLimitConfig;
import com.shopit.productservice.controller.ProductController;
import com.shopit.productservice.dto.ProductDtoRequest;
import com.shopit.productservice.dto.ProductDtoResponse;
import com.shopit.productservice.exception.RateLimitExceededException;
import com.shopit.productservice.service.ProductService;
import io.github.bucket4j.*;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

        @InjectMocks
        private ProductController productController;

        @Mock
        private ProductService productService;

        @Mock
        private RateLimitConfig rateLimitConfig;

        private MockMvc mockMvc;
        private static final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void mockMvcSetup() {
                mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

        }

        @Test
        void addProductTestValid() throws Exception {

                ProductDtoRequest productDtoRequest = ProductDtoRequest.builder()
                                .productName("OnePlus 12")
                                .description("High End Device")
                                .price(55000)
                                .build();

                // Set the value using reflection
                ReflectionTestUtils.setField(productController, "username", "gaurav");

                Mockito.when(productService.addProduct(any(ProductDtoRequest.class))).thenReturn(123);

                Bucket bucket = Bucket4j.builder()
                                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)))).build();

                Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);

                ResultActions resultAction = mockMvc.perform(post("/products/addProduct")
                                .content(objectMapper.writeValueAsString(productDtoRequest))
                                .contentType(MediaType.APPLICATION_JSON));

                resultAction.andExpect(status().isCreated());
                resultAction.andExpect(content().string("Added product with ID: 123"));
        }

        @Test
        void addProductTestInvalid() throws Exception {

                ProductDtoRequest productDtoRequest = ProductDtoRequest.builder()
                                .productName("OnePlus 12")
                                .description("High End Device")
                                .price(55000)
                                .build();

                // Set the value using reflection
                ReflectionTestUtils.setField(productController, "username", "gaurav");

                Bucket bucket = Bucket4j.builder()
                                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1)))).build();
                bucket.tryConsume(1);

                Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);

                Assertions.assertThrows(ServletException.class, () -> mockMvc.perform(post("/products/addProduct")
                        .content(objectMapper.writeValueAsString(productDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                        );
        }

        @Test
        void getAllProductsV1TestValid() throws Exception {

                List<ProductDtoResponse> productDtoResponseList = new ArrayList<>();
                productDtoResponseList.add(ProductDtoResponse.builder()
                                .productId(123)
                                .productName("OnePlus 12")
                                .description("High End Device")
                                .price(55000)
                                .build());

                // Set the value using reflection
                ReflectionTestUtils.setField(productController, "username", "gaurav");

                Mockito.when(productService.getAllProducts()).thenReturn(productDtoResponseList);

                Bucket bucket = Bucket4j.builder()
                                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)))).build();

                Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);

                ResultActions resultAction = mockMvc.perform(get("/products/getAllProducts")
                                .header("X-API-VERSION", "1")
                                .contentType(MediaType.APPLICATION_JSON));

                resultAction.andExpect(status().isOk());
                resultAction.andExpect(content().string(objectMapper.writeValueAsString(productDtoResponseList)));
        }

        @Test
        void getAllProductsV1TestInvalid() throws Exception {

                List<ProductDtoResponse> productDtoResponseList = new ArrayList<>();
                productDtoResponseList.add(ProductDtoResponse.builder()
                                .productId(123)
                                .productName("OnePlus 12")
                                .description("High End Device")
                                .price(55000)
                                .build());

                // Set the value using reflection
                ReflectionTestUtils.setField(productController, "username", "gaurav");

                Bucket bucket = Bucket4j.builder()
                                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1)))).build();
                bucket.tryConsume(1);

                Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);

                Assertions.assertThrows(ServletException.class, () -> mockMvc.perform(get("/products/getAllProducts")
                                .header("X-API-VERSION", "1")
                                .contentType(MediaType.APPLICATION_JSON))
                );
        }

        @Test
        void getAllProductsV2TestValid() throws Exception {

                List<ProductDtoResponse> productDtoResponseList = new ArrayList<>();
                productDtoResponseList.add(ProductDtoResponse.builder()
                                .productId(123)
                                .productName("OnePlus 12")
                                .description("High End Device")
                                .price(55000)
                                .build());

                // Set the value using reflection
                ReflectionTestUtils.setField(productController, "username", "gaurav");

                Mockito.when(productService.getAllProducts()).thenReturn(productDtoResponseList);

                Bucket bucket = Bucket4j.builder()
                                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)))).build();

                Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);

                ResultActions resultAction = mockMvc.perform(get("/products/getAllProducts")
                                .header("X-API-VERSION", "2")
                                .contentType(MediaType.APPLICATION_JSON));

                resultAction.andExpect(status().isOk());
                resultAction.andExpect(content().string(objectMapper.writeValueAsString(productDtoResponseList)));
        }

        @Test
        void getAllProductsV2TestInvalid() throws Exception {

                List<ProductDtoResponse> productDtoResponseList = new ArrayList<>();
                productDtoResponseList.add(ProductDtoResponse.builder()
                                .productId(123)
                                .productName("OnePlus 12")
                                .description("High End Device")
                                .price(55000)
                                .build());

                // Set the value using reflection
                ReflectionTestUtils.setField(productController, "username", "gaurav");

                Bucket bucket = Bucket4j.builder()
                                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1)))).build();
                bucket.tryConsume(1);

                Mockito.when(rateLimitConfig.resolveBucket(any(String.class))).thenReturn(bucket);

                Assertions.assertThrows(ServletException.class, () -> mockMvc.perform(get("/products/getAllProducts")
                                .header("X-API-VERSION", "2")
                                .contentType(MediaType.APPLICATION_JSON))
                );
        }

}
