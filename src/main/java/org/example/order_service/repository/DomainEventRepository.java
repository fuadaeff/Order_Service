package org.example.order_service.repository;

import org.example.order_service.model.DomainEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DomainEventRepository extends JpaRepository<DomainEvent, Long> {
    List<DomainEvent> findByStatus(String status);
}