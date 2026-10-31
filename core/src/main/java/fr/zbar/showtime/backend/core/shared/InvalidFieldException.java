package fr.zbar.showtime.backend.core.shared;

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

  public InvalidFieldException onField(String field) {
    return new InvalidFieldException(field, messages);
  }

  public String field() {
    return field;
  }

  public List<String> messages() {
    return messages;
  }
}
