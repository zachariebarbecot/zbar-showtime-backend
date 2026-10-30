package fr.zbar.showtime.backend.core.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

  private static final Pattern FORMAT = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

  public Email {
    value = value == null ? null : value.strip().toLowerCase(Locale.ROOT);
    if (value == null || !FORMAT.matcher(value).matches()) {
      throw new InvalidFieldException("email", "Email must be a valid address");
    }
  }
}
