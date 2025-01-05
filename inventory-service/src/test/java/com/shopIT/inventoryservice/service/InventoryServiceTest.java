package com.shopit.inventoryservice.service;

import com.shopit.inventoryservice.dto.InventoryDtoRequest;
import com.shopit.inventoryservice.dto.InventoryDtoResponse;
import com.shopit.inventoryservice.entity.InventoryEntity;
import com.shopit.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private InventoryRepository inventoryRepo;

    @Test
    void quantityInStockTest(){
        InventoryEntity inventoryEntity = InventoryEntity.builder()
                .id(1)
                .quantity(101)
                .skuCode("OnePlus 11R")
                .build();

        Mockito.when(inventoryRepo.findBySkuCodeIn(any(List.class))).thenReturn(List.of(inventoryEntity));

        List<InventoryDtoResponse> actualInventoryDtoResponseList = inventoryService.quantityInStock(List.of("OnePlus 11R"));

        assertEquals(1, actualInventoryDtoResponseList.size());
    }

    @Test
    void addInInventoryTestEntityPresent(){
        InventoryEntity inventoryEntity = InventoryEntity.builder()
                .id(1)
                .quantity(101)
                .skuCode("OnePlus 11R")
                .build();

        Mockito.when(inventoryRepo.findBySkuCode(any(String.class))).thenReturn(Optional.of(inventoryEntity));

        InventoryDtoRequest inventoryDtoRequest = InventoryDtoRequest.builder()
                .quantity(101)
                .skuCode("OnePlus 11R")
                .build();

        Integer actualId = inventoryService.addInInventory(inventoryDtoRequest);
        Integer expectedId = 1;

        assertEquals(expectedId, actualId);
    }

    @Test
    void addInInventoryTestEntityNotPresent(){
        Mockito.when(inventoryRepo.findBySkuCode(any(String.class))).thenReturn(Optional.ofNullable(null));

        InventoryDtoRequest inventoryDtoRequest = InventoryDtoRequest.builder()
                .quantity(101)
                .skuCode("OnePlus 11R")
                .build();

        // When we use save() method, it saves and populate the id in the same object
        // that is passed to it, therefore if we do it normally ie thenReturn(), then it
        // wont work unless we
        // do, productEntity = productRepo.save(productEntity); in the service layer.
        // Therefore, we use this way which capture the passed object and update it as
        // required.
        ArgumentCaptor<InventoryEntity> captor = ArgumentCaptor.forClass(InventoryEntity.class);

        Mockito.when(inventoryRepo.save(captor.capture())).thenAnswer(invocation -> {
            // Get the Product entity passed to the save method
            InventoryEntity savedProduct = captor.getValue();

            // Manually set the productId field (assuming it's a Long type)
            savedProduct.setId(1);

            // Return the modified Product entity
            return savedProduct;
        });


        Integer actualId = inventoryService.addInInventory(inventoryDtoRequest);
        Integer expectedId = 1;

        assertEquals(expectedId, actualId);
    }
}