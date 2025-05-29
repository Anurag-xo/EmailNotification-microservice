package com.notrlyanurag.ws.emailnotification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Value("${spring.kafka.properties.max.in.flight.requests.per.connection}")
private Integer inflightRequests;

@SpringBootTest
class EmailNotificationMicroserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
