package org.example.order_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull
    private Long customerId;

    @NotNull
    @Size(min = 1)
    private List<OrderItemRequest> items;
}
