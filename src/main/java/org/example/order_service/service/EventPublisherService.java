package org.example.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_service.model.DomainEvent;
import org.example.order_service.repository.DomainEventRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final DomainEventRepository domainEventRepository;
    private final ObjectMapper objectMapper;

    public void publish(String type, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            DomainEvent event = DomainEvent.builder()
                    .type(type)
                    .payload(json)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();
            domainEventRepository.save(event);
            log.info("Event saved: {} ", type);
        } catch (Exception e) {
            log.error("Failed to save event: {}", e.getMessage());
        }
    }
}