package com.example.eventhub.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;

@Configuration
public class AmqpConfig {
  public static final String EXCHANGE = "events.exchange";
  public static final String DLX = "events.dlx";
  public static final String QUEUE_MAIN = "events.main";
  public static final String QUEUE_DLQ = "events.dlq";

  @Bean
  public TopicExchange eventsExchange() {
    return new TopicExchange(EXCHANGE, true, false);
  }

  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange(DLX, true, false);
  }

  @Bean
  public Queue mainQueue() {
    return QueueBuilder.durable(QUEUE_MAIN)
        .withArgument("x-dead-letter-exchange", DLX)
        .withArgument("x-dead-letter-routing-key", QUEUE_DLQ)
        .build();
  }

  @Bean
  public Queue dlq() {
    return QueueBuilder.durable(QUEUE_DLQ).build();
  }

  @Bean
  public Binding mainBinding() {
    return BindingBuilder.bind(mainQueue()).to(eventsExchange()).with("#");
  }

  @Bean
  public Binding dlqBinding() {
    return BindingBuilder.bind(dlq()).to(deadLetterExchange()).with(QUEUE_DLQ);
  }

  @Bean
  public MessageConverter messageConverter() {
    return new SimpleMessageConverter();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory cf, RabbitTemplate tpl) {
    var factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(cf);
    factory.setDefaultRequeueRejected(false); // 失敗時に即リクイーイングしない（→DLQへ）
    factory.setAdviceChain(
        RetryInterceptorBuilder.stateless()
            .maxAttempts(5)
            .backOffOptions(2000, 2.0, 15000) // 初回2s, 倍率2倍, 最大15s
            .build());
    return factory;
  }

  @Bean
  public MessageRecoverer deadLetterRecoverer() {
    // 失敗し続けたメッセージは DLQ へ
    return new RejectAndDontRequeueRecoverer();
  }
}