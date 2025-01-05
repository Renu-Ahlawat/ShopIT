package com.shopit.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineItemsDtoResponse implements Serializable { // Implements Serializable because Redis supports
                                                                 // serialised data only ie in the form of byte stream.

    private Integer id;
    private String skuCode;
    private Integer price;
    private Integer quantity;

}