package com.shopit.inventoryservice.repository;

import com.shopit.inventoryservice.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Integer> {

    List<InventoryEntity> findBySkuCodeIn(List<String> skuCode);

    Optional<InventoryEntity> findBySkuCode(String skuCode);
}
