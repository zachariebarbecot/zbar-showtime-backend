package fr.zbar.showtime.backend.core.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

  private static final Pattern FORMAT = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

  private static final String INVALID_MESSAGE = "Email must be a valid address";

  public Email {
    if (value == null) {
      throw new InvalidFieldException("email", INVALID_MESSAGE);
    }
    value = value.strip().toLowerCase(Locale.ROOT);
    if (!FORMAT.matcher(value).matches()) {
      throw new InvalidFieldException("email", INVALID_MESSAGE);
    }
  }

  @Override
  public String toString() {
    return "Email[****]";
  }
}
