package com.notrlyanurag.ws.emailnotification;

import com.notrlyanurag.ws.core.ProductCreatedEvent;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

@EmbeddedKafka
@SpringBootTest(
    properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductCreatedEventHandlerIntegrationTest {

  @MockitoBean ProcessedEventRepository processedEventRepository;

  @MockitoBean RestTemplate restTemplate;

  @Test
  public void testProductCreatedEventHandler_OnProductCreated_HandlesEvent() {
    // Arrange
    ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
    productCreatedEvent.setPrice(new BigDecimal(10));
    productCreatedEvent.setProductId(UUID.randomUUID().toString());
    productCreatedEvent.setQuantity(1);
    productCreatedEvent.setTitle("Test product");

    String messageId = UUID.randomUUID().toString();
    String messageKey = productCreatedEvent.getProductId();

    ProducerRecord<String, ProductCreatedEvent> record =
        new ProducerRecord<>("products-created-events-topic", messageKey, productCreatedEvent);

    record.headers().add("messageId", messageId.getBytes());
    record.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

    ProcessedEventEntity processedEventEntity = new ProcessedEventEntity();
    when(ProcessedEventRepository.findByMessageId(anyString())).thenReturn(processedEventEntity);
    when(ProcessedEventRepository.save(any(ProcessedEventEntity.class))).thenReturn(null);

    String responseBody = "{\"key\":\"value\"}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> responseEntity =
        new ResponseEntity<>(responseBody, headers, HttpStatus.OK);

    when(RestTemplate.exchange(
            any(String.class), any(HttpMethod.class), isNull(), eq(String.class)))
        .thenReturn(responseEntity);

    // Act

    // Assert
  }
}
