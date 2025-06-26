package com.notrlyanurag.ws.emailnotification;

public class RetryableException extends RuntimeException {

  public RetryableException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  public RetryableException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }
}
