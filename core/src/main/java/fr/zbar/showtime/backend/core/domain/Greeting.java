package fr.zbar.showtime.backend.core.domain;

import java.util.UUID;

public class Greeting extends AggregateRoot<UUID> {

  public Greeting(UUID id, String message) {}

  public Greeting(String message) {}

  public UUID id() {
    return null;
  }

  public String message() {
    return null;
  }
}
