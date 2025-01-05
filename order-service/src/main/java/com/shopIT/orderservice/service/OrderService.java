package com.shopit.orderservice.service;

import com.shopit.orderservice.constants.OrderConstants;
import com.shopit.orderservice.dto.*;
import com.shopit.orderservice.entity.OrderEntity;
import com.shopit.orderservice.entity.OrderLineItemsEntity;
import com.shopit.orderservice.exception.InventoryNotReachableException;
import com.shopit.orderservice.exception.OrderNotFoundException;
import com.shopit.orderservice.exception.OrderNotSavedException;
import com.shopit.orderservice.exception.ProductNotInStockException;
import com.shopit.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service // Used to represent a class as business logic handling class and also mark this
// as @Component so spring IOC container
// create and inject this class's object wherever it's needed like in controller
// class using @Autowired
@Slf4j // Given by lombok for logging purpose
public class OrderService {

    private final OrderRepository orderRepo;

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public OrderService(OrderRepository orderRepo, WebClient.Builder webClientBuilder) {
        this.orderRepo = orderRepo;
        this.webClientBuilder = webClientBuilder;
    }

    // Ideally, We do not use caching in case of POST operation.
    @CircuitBreaker(name = "inventoryCall", fallbackMethod = "placeOrderFallBack")
    public Integer placeOrder(final OrderDtoRequest orderDtoRequest) {
        // First check if the products with ordered quantity are in stock or not.
        // If they are in stock, place the order otherwise don't.

        // IMPORTANT:
        // A. If we loop through all the products/skuCodes in the order and check if
        // they are in stock by calling the
        // inventory service one by one, it will be slower and costly.
        // B. So instead we will send all the skuCodes/products in single call to the
        // inventory service and collect
        // the quantity available in stock for each product/skuCode in a list.

        final List<String> skuCode = orderDtoRequest.getOrderLineItemsDtoRequestList().stream()
                .map(orderLineItem -> orderLineItem.getSkuCode()).toList();

        // Alternative of RestTemplate and introduced in Spring 5.
        // If we do not want load balancing then we can directly create webClient like
        // below without making config class
        // final WebClient webClient = WebClient.create();

        ResponseEntity<List<InventoryDtoResponse>> inventoryDtoResponseREntity = webClientBuilder.build()
                .get()
                // .uri("http://localhost:2334/shopit/inventory/quantity", uriBuilder ->
                // uriBuilder
                .uri("http://inventory-service/shopit/inventory/quantity", uriBuilder -> uriBuilder
                        .queryParam("skuCode", skuCode)
                        .build())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<InventoryDtoResponse>>() {
                }) // Used when its returning-
                // -Response entity otherwise we use .bodyToMono()
                .block();

        log.info(OrderConstants.INVENTORY_REACHABLE); // provided by @Slf4j

        // Converting into stream for efficient filtering in next steps
        final Stream<InventoryDtoResponse> inventoryDtoResponseStream = inventoryDtoResponseREntity.getBody()
                .stream();

        // Since we called the inventory service only once and got all the required
        // product's quantity
        // we need to now loop through the orderLineItem list in the order to check if
        // each ordered product
        // with quantity 'x' is available in the inventory or not.

        // Here, orderLineItem contains ordered products with required quantity and
        // InventoryDtoResponse Stream contains all the ordered products with the
        // available quantity
        for (OrderLineItemsDtoRequest orderLineItem : orderDtoRequest.getOrderLineItemsDtoRequestList()) {
            final Optional<InventoryDtoResponse> inventoryDtoResponseSame = inventoryDtoResponseStream
                    .filter(inventoryDtoResponse -> inventoryDtoResponse.getSkuCode()
                            .equals(orderLineItem.getSkuCode())) // Filtered to get the
                    // matching skuCode
                    // corresponding to the one
                    // in orderLineItem
                    .findFirst(); // to collect it

            // If inventoryDtoResponseSame is empty meaning the ordered product is not
            // present in any inventory
            // or If the available quantity of any product is less than the required
            // quantity in the order then
            // return exception as product(s) not in stock.
            if (inventoryDtoResponseSame.isEmpty()
                    || inventoryDtoResponseSame.get().getQuantity() < orderLineItem.getQuantity()) {
                log.info(OrderConstants.PRODUCT_NOT_IN_STOCK);
                throw new ProductNotInStockException(OrderConstants.PRODUCT_NOT_IN_STOCK);
            }
        }

        // Further statements will be executed only when all the products in order are
        // present in the inventory with
        // the required quantity.

        // Placing the order
        final OrderEntity orderEntity = OrderEntity.builder()
                .orderNumber(UUID.randomUUID().toString()) // Random number generation
                .build();

        final List<OrderLineItemsEntity> orderLineItemsEntityList = orderDtoRequest
                .getOrderLineItemsDtoRequestList()
                .stream().map(orderLineItem -> orderLIDtoReqToOLIEntity(orderLineItem)).toList();

        orderEntity.setOrderLineItemsList(orderLineItemsEntityList);

        try{
            orderRepo.save(orderEntity);
        }
        catch (Exception ex){
            log.error(OrderConstants.ORDER_NOT_SAVED);
            throw new OrderNotSavedException(OrderConstants.ORDER_NOT_SAVED);
        }

        log.info(OrderConstants.PLACED_ORDER + orderEntity.getId()); // provided by @Slf4j

        return orderEntity.getId();
    }

    public Integer placeOrderFallBack(final OrderDtoRequest orderDtoRequest, RuntimeException runtimeException) {
        log.warn(OrderConstants.INVENTORY_UNREACHABLE);
        throw new InventoryNotReachableException(OrderConstants.INVENTORY_UNREACHABLE);
    }

    // Cacheable Annotation includes:.
    // 1. key = "#orderId": Specifies the key used for caching. In this case, the
    // orderId parameter value is
    // used as the key for caching the method's results. This means that if the
    // method is called with the same
    // orderId value multiple times, the cached result will be returned instead of
    // executing the method again.
    // 2. value or cacheName: attributes serve the same purpose: they specify the
    // name of the cache where the method's
    // results should be stored or retrieved. However, the value attribute is the
    // preferred way to specify the cache name.
    // In this case, Serialized OrderDtoResponse objects would be stored in "orders"
    // cache with the key "orderId".
    @Cacheable(key = "#orderId", value = "orders")
    public OrderDtoResponse getOrderDetails(final Integer orderId) {
        final Optional<OrderEntity> orderEntityOpt = orderRepo.findById(orderId);

        if (orderEntityOpt.isEmpty()) {
            log.info(OrderConstants.ORDER_404);
            throw new OrderNotFoundException(OrderConstants.ORDER_404);
        }

        final OrderEntity orderEntity = orderEntityOpt.get();

        final OrderDtoResponse orderDtoResponse = OrderDtoResponse.builder()
                .id(orderEntity.getId())
                .orderNumber(orderEntity.getOrderNumber())
                .build();

        final List<OrderLineItemsDtoResponse> orderLineItemsDtoResponseList = orderEntity
                .getOrderLineItemsList()
                .stream().map(orderLineItem -> orderLIEntityToOLIDtoRes(orderLineItem)).toList();

        orderDtoResponse.setOrderLineItemsDtoResponseList(orderLineItemsDtoResponseList);

        return orderDtoResponse;
    }

    public OrderLineItemsDtoResponse orderLIEntityToOLIDtoRes(final OrderLineItemsEntity orderLineItemsEntity) {
        return OrderLineItemsDtoResponse.builder()
                .id(orderLineItemsEntity.getId())
                .price(orderLineItemsEntity.getPrice())
                .quantity(orderLineItemsEntity.getQuantity())
                .skuCode(orderLineItemsEntity.getSkuCode())
                .build();
    }

    public OrderLineItemsEntity orderLIDtoReqToOLIEntity(final OrderLineItemsDtoRequest orderLineItemsDtoRequest) {
        return OrderLineItemsEntity.builder()
                .skuCode(orderLineItemsDtoRequest.getSkuCode())
                .quantity(orderLineItemsDtoRequest.getQuantity())
                .price(orderLineItemsDtoRequest.getPrice())
                .build();
    }
}