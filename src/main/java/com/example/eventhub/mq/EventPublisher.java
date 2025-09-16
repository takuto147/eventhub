package com.example.eventhub.mq;


import com.example.eventhub.config.AmqpConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Component
public class EventPublisher {
private final RabbitTemplate rabbitTemplate;
public EventPublisher(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }
public void publish(String routingKey, String payload) {
rabbitTemplate.convertAndSend(AmqpConfig.EXCHANGE, routingKey, payload);
}
}