package org.example.order_service.service;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import org.example.order_service.exception.OrderException;
import org.example.order_service.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PdfGeneratorService {

    @Value("${invoice.storage.path}")
    private String storagePath;

    public String generateInvoicePdf(Order order) {
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
            document.add(new Paragraph("Date: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
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
            throw new OrderException("Failed to generate PDF: " + e.getMessage());
        }

        return filePath;
    }
}