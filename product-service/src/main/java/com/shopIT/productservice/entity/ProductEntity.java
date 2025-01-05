package com.shopit.productservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // This will be the equivalent of @Getters, @Setters, @ToString,
      // @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // To implement Builder Design
@Entity // This is used to map this class to relational DB table. @Document is used for
        // non-relational DB like mongoDB
@SequenceGenerator(name = "seq", initialValue = 100, allocationSize = 1) // This is used to generate the ID as required
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    private Integer productId;
    private String productName;
    private String description;
    private Integer price;
}