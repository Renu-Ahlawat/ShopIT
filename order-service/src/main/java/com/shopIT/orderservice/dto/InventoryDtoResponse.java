package com.shopit.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // This will be the equivalent of @Getters, @Setters, @ToString,
      // @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // To implement Builder Design
public class InventoryDtoResponse {

        private Integer id;
        private String skuCode;
        private Integer quantity;

}
