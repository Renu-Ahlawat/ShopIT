package com.shopit.productservice.repository;

import com.shopit.productservice.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// Since we are using JpaRepository, we do not need to add @Repository here because JpaRepository includes it already
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
}