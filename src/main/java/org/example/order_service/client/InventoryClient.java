package org.example.order_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_service.dto.ProductDto;
import org.example.order_service.exception.OrderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryUrl;

    public ProductDto getProduct(Long productId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ProductDto> response = restTemplate.exchange(
                    inventoryUrl + "/products/" + productId,
                    HttpMethod.GET,
                    entity,
                    ProductDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new OrderException("Product not found: " + productId);
        }
    }
}