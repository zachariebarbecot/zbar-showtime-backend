package fr.zbar.showtime.backend.core.user.domain;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record RawPassword(String value) {

  private static final int MIN_LENGTH = 12;
  private static final int MAX_LENGTH = 64;
  private static final int MAX_BYTES = 72;

  public RawPassword {
    if (value == null) {
      throw new InvalidFieldException("password", "Password must be provided");
    }
  }

  public List<String> violations(Email email) {
    List<String> violations = new ArrayList<>();
    if (value.length() < MIN_LENGTH) {
      violations.add("Password must be at least %d characters".formatted(MIN_LENGTH));
    }
    if (value.length() > MAX_LENGTH) {
      violations.add("Password must be at most %d characters".formatted(MAX_LENGTH));
    }
    if (value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
      violations.add("Password must be at most %d bytes in UTF-8".formatted(MAX_BYTES));
    }
    if (value.chars().noneMatch(Character::isUpperCase)) {
      violations.add("Password must contain an uppercase letter");
    }
    if (value.chars().noneMatch(Character::isLowerCase)) {
      violations.add("Password must contain a lowercase letter");
    }
    if (value.chars().noneMatch(Character::isDigit)) {
      violations.add("Password must contain a digit");
    }
    if (value.chars().allMatch(Character::isLetterOrDigit)) {
      violations.add("Password must contain a non-alphanumeric character");
    }
    if (value.toLowerCase(Locale.ROOT).contains(email.value())) {
      violations.add("Password must not contain the email");
    }
    return violations;
  }

  @Override
  public String toString() {
    return "RawPassword[****]";
  }
}
