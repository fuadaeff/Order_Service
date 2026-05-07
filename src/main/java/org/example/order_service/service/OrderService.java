package org.example.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_service.dto.OrderItemResponse;
import org.example.order_service.dto.OrderRequest;
import org.example.order_service.dto.OrderResponse;
import org.example.order_service.dto.ProductDto;
import org.example.order_service.exception.OrderException;
import org.example.order_service.model.Order;
import org.example.order_service.model.OrderItem;
import org.example.order_service.model.OrderStatus;
import org.example.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockReservationService stockReservationService;
    private final InvoiceService invoiceService;
    private final EventPublisherService eventPublisherService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String token) {
        Map<Long, ProductDto> productMap =
                stockReservationService.validateAndFetchProducts(request.getItems(), token);

        var total = stockReservationService.calculateTotal(request.getItems(), productMap);

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.CONFIRMED)
                .totalAmount(total)
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            ProductDto product = productMap.get(itemReq.getProductId());
            return OrderItem.builder()
                    .order(order)
                    .productId(itemReq.getProductId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
        }).collect(Collectors.toList());

        order.setItems(items);
        Order saved = orderRepository.save(order);

        invoiceService.generateInvoice(saved);

        eventPublisherService.publish("ORDER_CREATED", toResponse(saved));

        return toResponse(saved);
    }

    public OrderResponse getById(Long id) {
        return toResponse(findById(id));
    }

    public List<OrderResponse> getByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found: " + id));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item ->
                OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(
                                java.math.BigDecimal.valueOf(item.getQuantity())))
                        .build()
        ).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}