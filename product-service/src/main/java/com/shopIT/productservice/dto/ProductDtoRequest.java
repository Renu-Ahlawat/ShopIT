package com.shopit.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // This will be the equivalent of @Getters, @Setters, @ToString,
      // @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // To implement Builder Design - search this
public class ProductDtoRequest {

    private String productName;

    private String description;

    private Integer price;
}

// We should make Request and Response classes for each model because it is a
// good practice since if we need to
// hide some data or give additional back to the postman/client.