package org.example.order_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer stockQty;
}
