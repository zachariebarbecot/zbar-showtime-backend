package fr.zbar.showtime.backend.core.user.domain;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public record Email(String value) {

  private static final Pattern FORMAT = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

  private static final int MAX_LENGTH = 254;

  private static final String INVALID_MESSAGE = "Email must be a valid address";

  public Email {
    if (StringUtils.isBlank(value)) {
      throw new InvalidFieldException("email", INVALID_MESSAGE);
    }
    value = value.strip().toLowerCase(Locale.ROOT);
    if (value.length() > MAX_LENGTH) {
      throw new InvalidFieldException(
          "email", "Email must be at most %d characters".formatted(MAX_LENGTH));
    }
    if (!FORMAT.matcher(value).matches()) {
      throw new InvalidFieldException("email", INVALID_MESSAGE);
    }
  }

  @Override
  public String toString() {
    return "Email[****]";
  }
}
