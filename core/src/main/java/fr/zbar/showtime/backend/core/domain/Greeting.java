package fr.zbar.showtime.backend.core.domain;

import java.util.UUID;

public class Greeting extends AggregateRoot<UUID> {

  private final String message;

  public Greeting(UUID id, String message) {
    super(id);
    if (message.isBlank()) {
      throw new InvalidGreetingException("Message cannot be blank");
    }
    this.message = message;
  }

  public Greeting(String message) {
    this(UUID.randomUUID(), message);
  }

  public String message() {
    return message;
  }

  public static class InvalidGreetingException extends RuntimeException {

    public InvalidGreetingException(String message) {
      super(message);
    }
  }
}
