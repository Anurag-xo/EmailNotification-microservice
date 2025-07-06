package com.notrlyanurag.ws.emailnotification;

import com.notrlyanurag.ws.core.ProductCreatedEvent;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@KafkaListener(topics = "products-created-events-topic")
public class ProductCreatedEventHandler {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
  private RestTemplate restTemplate;
  private ProcessedEventRepository processedEventRepository;

  public ProductCreatedEventHandler(RestTemplate restTemplate, ProcessedEventRepository processedEventRepository) {
    this.restTemplate = restTemplate;
    this.processedEventRepository = processedEventRepository;
  }

  // Reading a unique message header id using @Header
  @Transactional
  @KafkaHandler
  public void handle(
      @Payload ProductCreatedEvent productCreatedEvent,
      @Header("messageId") String messageId,
      @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
    LOGGER.info(
        "Received a new event: "
            + productCreatedEvent.getTitle()
            + " with productId: "
            + productCreatedEvent.getProductId());

    // Check if this message was already processed before

    ProcessedEventEntity existingRecord = processedEventRepository.findByMessageId(messageId);

    if (existingRecord != null) {
      LOGGER.info("Found a duplicate message id: {}", existingRecord.getMessageId());
      return;
    }

    String requestUrl = "http://localhost:8082/response/200";

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);

      if (response.getStatusCode().value() == HttpStatus.OK.value()) {
        LOGGER.info("Received response from a remote service: " + response.getBody());
      }
    } catch (ResourceAccessException ex) {
      LOGGER.error(ex.getMessage());
      throw new RetryableException(ex);
    } catch (HttpServerErrorException ex) {
      LOGGER.error(ex.getMessage());
      throw new NotRetryableException(ex);
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage());
      throw new NotRetryableException(ex);
    }
    // Save a unique message id in a database table
    try {
      processedEventRepository.save(
          new ProcessedEventEntity(messageId, productCreatedEvent.getProductId()));
    } catch (DataIntegrityViolationException ex) {
      throw new NotRetryableException(ex);
    }
  }
}
