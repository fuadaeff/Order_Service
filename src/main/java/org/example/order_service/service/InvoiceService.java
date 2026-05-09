package org.example.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_service.dto.InvoiceResponse;
import org.example.order_service.exception.OrderException;
import org.example.order_service.model.Invoice;
import org.example.order_service.model.Order;
import org.example.order_service.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PdfGeneratorService pdfGeneratorService;

    public InvoiceResponse generateInvoice(Order order) {
        String filePath = pdfGeneratorService.generateInvoicePdf(order);
        String invoiceNumber = filePath
                .substring(filePath.lastIndexOf("/") + 1)
                .replace(".pdf", "");

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .orderId(order.getId())
                .filePath(filePath)
                .createdAt(LocalDateTime.now())
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        return toResponse(saved);
    }

    public InvoiceResponse getByOrderId(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderException("Invoice not found for order: " + orderId));
        return toResponse(invoice);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(invoice.getOrderId())
                .filePath(invoice.getFilePath())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
