package com.shopit.inventoryservice.repository;

import com.shopit.inventoryservice.entity.TPSEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TPSRepository extends JpaRepository<TPSEntity, String> {
    Optional<TPSEntity> findByUsername(String key);
}
