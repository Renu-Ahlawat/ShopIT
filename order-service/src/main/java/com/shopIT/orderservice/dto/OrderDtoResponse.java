package com.shopit.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDtoResponse implements Serializable { // Implements Serializable because Redis supports serialised
                                                        // data only ie in the form of byte stream.
    private Integer id;
    private String orderNumber;
    private List<OrderLineItemsDtoResponse> orderLineItemsDtoResponseList;
}