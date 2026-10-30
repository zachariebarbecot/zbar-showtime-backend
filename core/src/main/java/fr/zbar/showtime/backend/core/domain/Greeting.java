package fr.zbar.showtime.backend.core.domain;

import java.util.UUID;

public class Greeting extends AggregateRoot<UUID> {

  private final String message;

  public Greeting(UUID id, String message) {
    super(id);
    this.message = message;
  }

  public Greeting(String message) {
    this(UUID.randomUUID(), message);
  }

  public String message() {
    return message;
  }
}
