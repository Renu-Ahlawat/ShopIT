package com.shopit.orderservice.service;

import com.shopit.orderservice.dto.*;
import com.shopit.orderservice.entity.OrderEntity;
import com.shopit.orderservice.entity.OrderLineItemsEntity;
import com.shopit.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private WebClient.Builder webClientBuilderMock;

    @Mock
    private WebClient webClientMock;

    @Test
    @Disabled
    void placeOrderTest() {
        OrderLineItemsDtoRequest orderLineItemsDtoRequest = OrderLineItemsDtoRequest.builder()
                .price(1)
                .quantity(1)
                .skuCode("OnePlus 11R")
                .build();

        List<OrderLineItemsDtoRequest> orderLineItemsDtoRequestList= new ArrayList<>();
        orderLineItemsDtoRequestList.add(orderLineItemsDtoRequest);

        OrderDtoRequest orderDtoRequest = OrderDtoRequest.builder()
                .orderLineItemsDtoRequestList(orderLineItemsDtoRequestList)
                .build();

        final List<String> skuCode = orderDtoRequest.getOrderLineItemsDtoRequestList().stream()
                .map(orderLineItem -> orderLineItem.getSkuCode()).toList();

        List<InventoryDtoResponse> inventoryDtoResponseList = new ArrayList<>();
        inventoryDtoResponseList.add(InventoryDtoResponse.builder()
                        .id(1)
                        .quantity(101)
                        .skuCode("OnePlus 11R")
                .build());

//        Mockito.when(any(WebClient.class))
//                .thenReturn((WebClient) ResponseEntity.ok().body(inventoryDtoResponseList));

        // When we use save() method, it saves and populate the id in the same object
        // that is passed to it, therefore if we do it normally ie thenReturn(), then it
        // wont work unless we
        // do, productEntity = productRepo.save(productEntity); in the service layer.
        // Therefore, we use this way which capture the passed object and update it as
        // required.
        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);

        Mockito.when(orderRepo.save(captor.capture())).thenAnswer(invocation -> {
            // Get the Product entity passed to the save method
            OrderEntity savedProduct = captor.getValue();

            // Manually set the productId field (assuming it's a Long type)
            savedProduct.setId(1);

            // Return the modified Product entity
            return savedProduct;
        });

        Integer actualId = orderService.placeOrder(orderDtoRequest);
        Integer expectedId = 1;

        assertEquals(expectedId, actualId);
    }

    @Test
    void getOrderDetailsTest() {
        Integer orderId = 1;
        OrderEntity orderEntity = OrderEntity.builder()
                .id(1)
                .orderLineItemsList(new ArrayList<>())
                .orderNumber("123")
                .build();

        Mockito.when(orderRepo.findById(orderId)).thenReturn(Optional.of(orderEntity));

        Integer expectedOrderId = 1;
        OrderDtoResponse orderDtoResponse = orderService.getOrderDetails(orderId);

        assertEquals(expectedOrderId, orderDtoResponse.getId());
    }

    @Test
    void orderLIEntityToOLIDtoResTest() {
        OrderLineItemsEntity orderLineItemsEntity = OrderLineItemsEntity.builder()
                .id(1)
                .price(1000)
                .quantity(100)
                .skuCode("OnePlus 11R")
                .build();

        OrderLineItemsDtoResponse orderLineItemsDtoResponse = orderService.orderLIEntityToOLIDtoRes(orderLineItemsEntity);

        Integer expectedId = 1;
        Integer expectedPrice = 1000;
        Integer expectedQuantity = 100;
        String expectedSkuCode = "OnePlus 11R";

        assertEquals(expectedId, orderLineItemsDtoResponse.getId());
        assertEquals(expectedPrice, orderLineItemsDtoResponse.getPrice());
        assertEquals(expectedQuantity, orderLineItemsDtoResponse.getQuantity());
        assertEquals(expectedSkuCode, orderLineItemsDtoResponse.getSkuCode());
    }

    @Test
    void orderLIDtoReqToOLIEntity() {
        OrderLineItemsDtoRequest orderLineItemsDtoRequest = OrderLineItemsDtoRequest.builder()
                .price(1000)
                .quantity(100)
                .skuCode("OnePlus 11R")
                .build();

        OrderLineItemsEntity orderLineItemsEntity = orderService.orderLIDtoReqToOLIEntity(orderLineItemsDtoRequest);

        Integer expectedPrice = 1000;
        Integer expectedQuantity = 100;
        String expectedSkuCode = "OnePlus 11R";

        assertEquals(expectedPrice, orderLineItemsEntity.getPrice());
        assertEquals(expectedQuantity, orderLineItemsEntity.getQuantity());
        assertEquals(expectedSkuCode, orderLineItemsEntity.getSkuCode());
    }
}