package com.example.eventhub.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inbound_events", uniqueConstraints = @UniqueConstraint(columnNames = { "source", "event_id" }))
public class InboundEvent {
  @Id
  @GeneratedValue
  private UUID id;

  private String source;

  @Column(name = "event_id")
  private String eventId;

  @Column(columnDefinition = "jsonb")
  private String payload;

  @Column(name = "signature_ok")
  private boolean signatureOk;

  @Column(name = "received_at")
  private OffsetDateTime receivedAt = OffsetDateTime.now();

  // getters/setters ...
}