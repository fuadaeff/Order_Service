package org.example.order_service.service;


import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_service.dto.InvoiceResponse;
import org.example.order_service.exception.OrderException;
import org.example.order_service.model.Invoice;
import org.example.order_service.model.Order;
import org.example.order_service.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Value("${invoice.storage.path}")
    private String storagePath;

    public InvoiceResponse generateInvoice(Order order) {
        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String fileName = invoiceNumber + ".pdf";
        String filePath = storagePath + fileName;

        try {
            new File(storagePath).mkdirs();

            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("INVOICE").setBold().setFontSize(20));
            document.add(new Paragraph("Invoice Number: " + invoiceNumber));
            document.add(new Paragraph("Order ID: " + order.getId()));
            document.add(new Paragraph("Customer ID: " + order.getCustomerId()));
            document.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
            document.add(new Paragraph(" "));

            Table table = new Table(3);
            table.addCell("Product ID");
            table.addCell("Quantity");
            table.addCell("Unit Price");

            order.getItems().forEach(item -> {
                table.addCell(String.valueOf(item.getProductId()));
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(item.getUnitPrice().toString());
            });

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total: " + order.getTotalAmount()).setBold());

            document.close();

        } catch (Exception e) {
            throw new OrderException("Failed to generate invoice: " + e.getMessage());
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .orderId(order.getId())
                .filePath(filePath)
                .createdAt(LocalDateTime.now())
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        return InvoiceResponse.builder()
                .id(saved.getId())
                .invoiceNumber(saved.getInvoiceNumber())
                .orderId(saved.getOrderId())
                .filePath(saved.getFilePath())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public InvoiceResponse getByOrderId(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderException("Invoice not found for order: " + orderId));
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(invoice.getOrderId())
                .filePath(invoice.getFilePath())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
