package com.notrlyanurag.ws.emailnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class EmailNotificationMicroserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(EmailNotificationMicroserviceApplication.class, args);
  }

  @Bean
  RestTemplate getRestTemplate() {
    return new RestTemplate();
  }
}
