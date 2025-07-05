package com.notrlyanurag.ws.emailnotification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import com.notrlyanurag.ws.core.ProductCreatedEvent;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

@EmbeddedKafka
@SpringBootTest(
    properties = {"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
                  "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                  "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                  "spring.kafka.consumer.properties.spring.json.value.default.type=com.notrlyanurag.ws.core.ProductCreatedEvent",
                  "spring.kafka.consumer.properties.spring.json.trusted.packages=*"})
public class ProductCreatedEventHandlerIntegrationTest {

  @Configuration
  static class KafkaTestConfiguration {

    @Bean
    public KafkaProperties kafkaProperties() {
      return new KafkaProperties();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(
        KafkaProperties kafkaProperties) {
      Map<String, Object> props = kafkaProperties.buildProducerProperties();
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
      return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
        ProducerFactory<String, Object> producerFactory) {
      return new KafkaTemplate<>(producerFactory);
    }
  }

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
    when(processedEventRepository.findByMessageId(anyString())).thenReturn(processedEventEntity);
    when(processedEventRepository.save(any(ProcessedEventEntity.class))).thenReturn(null);

    String responseBody = "{"key":"value"}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> responseEntity =
        new ResponseEntity<>(responseBody, headers, HttpStatus.OK);

    when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(String.class)))
        .thenReturn(responseEntity);

    // Act

    kafkaTemplate.send(record).get();

    // Assert
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(processedEventRepository, times(1)).findByMessageId(anyString());
      verify(processedEventRepository, times(1)).save(any(ProcessedEventEntity.class));
      verify(restTemplate, times(1)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(String.class));
    });
  }
}
