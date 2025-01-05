package com.shopit.inventoryservice.service;

import com.shopit.inventoryservice.constants.InventoryConstants;
import com.shopit.inventoryservice.dto.InventoryDtoRequest;
import com.shopit.inventoryservice.dto.InventoryDtoResponse;
import com.shopit.inventoryservice.entity.InventoryEntity;
import com.shopit.inventoryservice.exception.InventoryNotSavedException;
import com.shopit.inventoryservice.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepo;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    public List<InventoryDtoResponse> quantityInStock(final List<String> skuCode) {
        // When we have multiple values of a field, and we need all the records for each
        // field value then we can use
        // findBy<fieldName>In() function provided by Spring data which works same as
        // mysql's IN

        // NOTE: When declaring new method provided by SpringData, make sure you set the
        // correct return type of the
        // method in the repository interface

        return inventoryRepo.findBySkuCodeIn(skuCode).stream()
                .map(inventoryEntity -> InventoryDtoResponse.builder()
                        .skuCode(inventoryEntity.getSkuCode())
                        .id(inventoryEntity.getId())
                        .quantity(inventoryEntity.getQuantity())
                        .build())
                .toList();
    }

    public Integer addInInventory(final InventoryDtoRequest inventoryDtoRequest) {
        final Optional<InventoryEntity> inventoryEntityOptional = inventoryRepo
                .findBySkuCode(inventoryDtoRequest.getSkuCode());
        InventoryEntity inventoryEntity = null;

        // If no inventory exists which contains this product then we create a new
        // inventory with given product
        if (inventoryEntityOptional.isEmpty()) {
            log.info(InventoryConstants.EMPTY_INVENTORY);
            inventoryEntity = InventoryEntity.builder()
                    .quantity(inventoryDtoRequest.getQuantity())
                    .skuCode(inventoryDtoRequest.getSkuCode())
                    .build();
        }
        // If the product with skuCode already exists in any inventory then we will not
        // create new inventory for this
        // and will simply update the quantity of already existing inventory with the
        // product
        else {
            inventoryEntity = inventoryEntityOptional.get();
            Integer prevQuantity = inventoryEntity.getQuantity();
            inventoryEntity.setQuantity(prevQuantity + inventoryDtoRequest.getQuantity());
        }

        try{
            inventoryRepo.save(inventoryEntity);
        }
        catch (Exception ex){
            log.error(InventoryConstants.INVENTORY_NOT_SAVED);
            throw new InventoryNotSavedException(InventoryConstants.INVENTORY_NOT_SAVED);
        }
        log.info(InventoryConstants.ADDED_IN_INVENTORY + inventoryEntity.getId());

        return inventoryEntity.getId();
    }
}