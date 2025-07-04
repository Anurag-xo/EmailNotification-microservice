# Email Notification Microservice

This microservice is a Spring Boot application that listens for events from a Kafka topic and sends email notifications. It's designed with robust error handling, including retry and dead-letter queue mechanisms.

## Features

- **Kafka Consumer:** Consumes messages from a specified Kafka topic.
- **Event-Driven:** Reacts to `ProductCreatedEvent` messages.
- **Resilient:** Implements retry logic for transient errors and a dead-letter topic for persistent errors.
- **REST Communication:** Interacts with other services via REST APIs.
- **Idempotent Consumer:** Ensures that duplicate messages are not processed, making the service more robust.

## Prerequisites

Before you begin, ensure you have the following installed:

- [Java 17](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html) or later
- [Apache Maven](https://maven.apache.org/download.cgi)
- [Apache Kafka](https://kafka.apache.org/downloads)

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd EmailNotificationMicroservice
```

### 2. Configure the application

Update the `src/main/resources/application.properties` file with your Kafka broker information:

```properties
spring.kafka.consumer.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=product-created-events
spring.kafka.consumer.properties.spring.json.trusted.packages=com.notrlyanurag.ws.core
```

### 3. Build the project

```bash
./mvnw clean install
```

### 4. Run the application

```bash
java -jar target/EmailNotificationMicroservice-0.0.1-SNAPSHOT.jar
```

## Usage

This microservice listens to the `products-created-events-topic` Kafka topic. It expects messages in the following format:

```json
{
  "productId": "some-product-id",
  "title": "Product Title",
  "price": 10.0,
  "quantity": 100
}
```

When a message is received, the `ProductCreatedEventHandler` is triggered.

## Error Handling

The application has a sophisticated error handling mechanism:

- **RetryableException:** If a `RetryableException` is thrown (e.g., due to a temporary network issue), the application will retry processing the message a configurable number of times.
- **NotRetryableException:** If a `NotRetryableException` is thrown (e.g., due to a permanent error), the message is sent to a dead-letter topic for later analysis.
- **Dead-Letter Topic:** The dead-letter topic is named `products-created-events-topic.DLT`.

## Dependencies

This project uses the following key dependencies:

- **Spring Boot:** For building the application.
- **Spring for Apache Kafka:** For Kafka integration.
- **core:** A shared library containing the `ProductCreatedEvent` class.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](https://choosealicense.com/licenses/mit/)

