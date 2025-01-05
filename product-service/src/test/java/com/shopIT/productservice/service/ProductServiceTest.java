package com.shopit.productservice.service;

import com.shopit.productservice.dto.ProductDtoRequest;
import com.shopit.productservice.dto.ProductDtoResponse;
import com.shopit.productservice.entity.ProductEntity;
import com.shopit.productservice.repository.ProductRepository;
import com.shopit.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepo;

    @Test
    void addProductTest() {
        final ProductDtoRequest productDtoRequest = ProductDtoRequest.builder()
                .productName("Creta")
                .description("Hyundai Car")
                .price(1500000)
                .build();

        // When we use save() method, it saves and populate the id in the same object
        // that is passed to it, therefore if we do it normally ie thenReturn(), then it
        // wont work unless we
        // do, productEntity = productRepo.save(productEntity); in the service layer.
        // Therefore, we use this way which capture the passed object and update it as
        // required.
        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);

        Mockito.when(productRepo.save(captor.capture())).thenAnswer(invocation -> {
            // Get the Product entity passed to the save method
            ProductEntity savedProduct = captor.getValue();

            // Manually set the productId field (assuming it's a Long type)
            savedProduct.setProductId(123);

            // Return the modified Product entity
            return savedProduct;
        });

        Integer actualId = productService.addProduct(productDtoRequest);
        Integer expectedId = 123;

        assertEquals(expectedId, actualId);
    }

    @Test
    void getAllProductsTest() {
        final List<ProductEntity> productEntityList = new ArrayList<>();
        productEntityList.add(ProductEntity.builder()
                .productId(123)
                .productName("OnePlus 12R")
                .description("High End Device")
                .price(39999)
                .build());

        Mockito.when(productRepo.findAll()).thenReturn(productEntityList);

        List<ProductDtoResponse> productDtoResponseList = productService.getAllProducts();

        assertEquals(1, productDtoResponseList.size());
    }

    @Test
    void productDtoReqToEntityTest() {

        ProductDtoRequest productDtoRequest = ProductDtoRequest.builder()
                .productName("OnePlus 12")
                .description("High End Device")
                .price(55000)
                .build();

        ProductEntity productEntity = productService.productDtoReqToEntity(productDtoRequest);

        assertEquals(productDtoRequest.getDescription(), productEntity.getDescription());
        assertEquals(productDtoRequest.getProductName(), productEntity.getProductName());
        assertEquals(productDtoRequest.getPrice(), productEntity.getPrice());
    }

    @Test
    void productEntityToDtoResTest() {

        ProductEntity productEntity = ProductEntity.builder()
                .productId(123)
                .productName("OnePlus 12")
                .description("High End Device")
                .price(55000)
                .build();

        ProductDtoResponse productDtoResponse = productService.productEntityToDtoRes(productEntity);

        assertEquals(productDtoResponse.getDescription(), productEntity.getDescription());
        assertEquals(productDtoResponse.getProductName(), productEntity.getProductName());
        assertEquals(productDtoResponse.getPrice(), productEntity.getPrice());
        assertEquals(productDtoResponse.getProductId(), productEntity.getProductId());
    }
}