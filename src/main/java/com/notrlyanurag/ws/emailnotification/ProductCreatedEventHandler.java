package com.notrlyanurag.ws.emailnotification;

import com.notrlyanurag.ws.core.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "products-created-events-topic")
public class ProductCreatedEventHandler {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @KafkaHandler
  public void handle(ProductCreatedEvent productCreatedEvent) {
    LOGGER.info("Received a new event: " + productCreatedEvent.getTitle());
  }
}
