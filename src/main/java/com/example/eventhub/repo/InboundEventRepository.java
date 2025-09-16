package com.example.eventhub.repo;


import com.example.eventhub.model.InboundEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;


public interface InboundEventRepository extends JpaRepository<InboundEvent, UUID> {
Optional<InboundEvent> findBySourceAndEventId(String source, String eventId);
}