package org.example.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.example.order_service.dto.InvoiceResponse;
import org.example.order_service.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.getByOrderId(orderId));
    }
}
