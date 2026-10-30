package fr.zbar.showtime.backend.core.domain;

import java.util.List;

public class InvalidFieldException extends RuntimeException {

  private final String field;
  private final List<String> messages;

  public InvalidFieldException(String field, List<String> messages) {
    super(field + ": " + String.join(", ", messages));
    this.field = field;
    this.messages = List.copyOf(messages);
  }

  public InvalidFieldException(String field, String message) {
    this(field, List.of(message));
  }

  public String field() {
    return field;
  }

  public List<String> messages() {
    return messages;
  }
}
