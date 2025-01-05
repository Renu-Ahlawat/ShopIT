package com.shopit.productservice.service;

import com.shopit.productservice.constants.ProductConstants;
import com.shopit.productservice.dto.ProductDtoRequest;
import com.shopit.productservice.dto.ProductDtoResponse;
import com.shopit.productservice.entity.ProductEntity;
import com.shopit.productservice.exception.ProductNotFoundException;
import com.shopit.productservice.exception.ProductNotSavedException;
import com.shopit.productservice.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
// Used to represent a class as business logic handling class and also mark this
// as @Component so spring IOC container
// create and inject this class's object wherever it's needed like in controller
// class using @Autowired
@Slf4j // Given by lombok for logging purpose
public class ProductService {

    private final ProductRepository productRepo;

    @Autowired
    public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public Integer addProduct(final ProductDtoRequest productDtoRequest) {
        final ProductEntity productEntity = productDtoReqToEntity(productDtoRequest);
        try{
            productRepo.save(productEntity);
        }
        catch (Exception ex){
            log.error(ProductConstants.PRODUCT_NOT_SAVED);
            throw new ProductNotSavedException(ProductConstants.PRODUCT_NOT_SAVED);
        }

        log.info(ProductConstants.ADDED_PRODUCT + productEntity.getProductId()); // provided by @Slf4j

        return productEntity.getProductId();
    }

    // We use parameters for defining KEY in cacheable but if 0 parameters are there
    // then we do:
    // 1. FIXED Key: the key 'fixedXYZ' is a fixed key that is used for all calls to
    // the getAllProducts method.
    // This means that regardless of how many times you call getAllProducts, the
    // same cache key is used,
    // and the method results are cached under this key.
    // 2. DYNAMIC Key based: the cache key #root.methodName is based on the name of
    // the method (getAllProducts).
    // This means that each method with a different name will have its own cache
    // entry, even if the method
    // takes no parameters. This can be useful if you have multiple methods with
    // different names that take
    // no parameters, and you want to cache their results separately.
    // @Cacheable(key = "'fixedXYZ'", value = "products")
    @Cacheable(key = "#root.methodName", value = "products")
    public List<ProductDtoResponse> getAllProducts() {
        final List<ProductEntity> productEntityList = productRepo.findAll();

        if(productEntityList.isEmpty()){
            throw new ProductNotFoundException(ProductConstants.PRODUCT_NOT_FOUND);
        }

        // Using Stream for conversion in less LOC
        // List<ProductDtoResponse> productDtoResponses =
        // productEntity.stream().map(product ->
        // productEntityToDtoRes(product)).toList();

        // Can be written like this:
        return productEntityList.stream().map(this::productEntityToDtoRes).toList();
    }

    public ProductEntity productDtoReqToEntity(final ProductDtoRequest productDtoRequest) {
        // Using builder() provided by lombok to implement conversion in less LOC
        return ProductEntity.builder()
                .productName(productDtoRequest.getProductName())
                .price(productDtoRequest.getPrice())
                .description(productDtoRequest.getDescription())
                .build();
    }

    public ProductDtoResponse productEntityToDtoRes(final ProductEntity productEntity) {
        // Using builder() provided by lombok to implement conversion in less LOC
        return ProductDtoResponse.builder()
                .productId(productEntity.getProductId())
                .productName(productEntity.getProductName())
                .price(productEntity.getPrice())
                .description(productEntity.getDescription())
                .build();
    }
}
