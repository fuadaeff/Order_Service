package org.example.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_service.client.InventoryClient;
import org.example.order_service.dto.OrderItemRequest;
import org.example.order_service.dto.ProductDto;
import org.example.order_service.exception.OrderException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReservationService {

    private final InventoryClient inventoryClient;

    public Map<Long, ProductDto> validateAndFetchProducts(List<OrderItemRequest> items, String token) {
        Map<Long, ProductDto> productMap = new HashMap<>();

        for (OrderItemRequest item : items) {
            ProductDto product = inventoryClient.getProduct(item.getProductId(), token);

            if (product == null) {
                throw new OrderException("Product not found: " + item.getProductId());
            }
            if (product.getStockQty() < item.getQuantity()) {
                throw new OrderException("Insufficient stock for product: " + product.getName()
                        + " (available: " + product.getStockQty() + ", requested: " + item.getQuantity() + ")");
            }
            productMap.put(item.getProductId(), product);
        }
        return productMap;
    }

    public BigDecimal calculateTotal(List<OrderItemRequest> items, Map<Long, ProductDto> productMap) {
        return items.stream()
                .map(item -> productMap.get(item.getProductId()).getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}