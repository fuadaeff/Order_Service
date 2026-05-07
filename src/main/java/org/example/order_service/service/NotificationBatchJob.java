package org.example.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_service.model.DomainEvent;
import org.example.order_service.repository.DomainEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationBatchJob {

    private final DomainEventRepository domainEventRepository;
    private final ExecutorService executorService;

    @Scheduled(fixedDelay = 30000)
    public void processPendingEvents() {
        List<DomainEvent> pendingEvents = domainEventRepository.findByStatus("PENDING");

        if (pendingEvents.isEmpty()) return;

        log.info("Processing {} pending events", pendingEvents.size());

        for (DomainEvent event : pendingEvents) {
            executorService.submit(() -> {
                try {
                    log.info("Processing event: {} | payload: {}", event.getType(), event.getPayload());
                    event.setStatus("PROCESSED");
                    domainEventRepository.save(event);
                } catch (Exception e) {
                    log.error("Failed to process event {}: {}", event.getId(), e.getMessage());
                }
            });
        }
    }
}