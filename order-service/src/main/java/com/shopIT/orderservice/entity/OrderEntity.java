package com.shopit.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data // This will be the equivalent of @Getters, @Setters, @ToString,
      // @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // To implement Builder Design
@Entity // This is used to map this class to relational DB table. @Document is used for
        // non-relational DB like mongoDB
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderNumber;

    @OneToMany(cascade = CascadeType.ALL) // Learn about Relationships
    private List<OrderLineItemsEntity> orderLineItemsList;
}