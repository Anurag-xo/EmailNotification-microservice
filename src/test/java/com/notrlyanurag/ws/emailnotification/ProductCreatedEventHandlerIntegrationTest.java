package com.notrlyanurag.ws.emailnotification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.notrlyanurag.ws.core.ProductCreatedEvent;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
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

  @Autowired KafkaTemplate<String, Object> kafkaTemplate;

  @SpyBean ProductCreatedEventHandler productCreatedEventHandler;

  @Test
  public void testProductCreatedEventHandler_OnProductCreated_HandlesEvent() throws Exception {
    // Arrange
    ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
    productCreatedEvent.setPrice(new BigDecimal(10));
    productCreatedEvent.setProductId(UUID.randomUUID().toString());
    productCreatedEvent.setQuantity(1);
    productCreatedEvent.setTitle("Test product");

    String messageId = UUID.randomUUID().toString();
    String messageKey = productCreatedEvent.getProductId();

    ProducerRecord<String, Object> record =
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

    kafkaTemplate.send(record).get();
    // Assert

    ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<ProductCreatedEvent> eventCaptor =
        ArgumentCaptor.forClass(ProductCreatedEvent.class);

    verify(
        productCreatedEventHandler,
        timeout(5000)
            .times(1)
            .handler(
                eventCaptor.capture(),
                messageIdCaptor.capture(),
                messageIdCaptor.capture(),
                messageKeyCaptor.capture()));

    assertEquals(messageId, messageIdCaptor.getValue());
    assertEquals(messageKey, messageKeyCaptor.getValue());
    assertEquals(productCreatedEvent.getProductId(), eventCaptor.getValue().getProductId());
  }
}
