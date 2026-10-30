package fr.zbar.showtime.backend.core.domain;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public record RawPassword(String value) {

  private static final int MIN_LENGTH = 12;
  private static final int MAX_LENGTH = 64;
  private static final int MAX_BYTES = 72;

  public List<String> violations(Email email) {
    List<String> violations = new ArrayList<>();
    if (value.length() < MIN_LENGTH) {
      violations.add("Password must be at least 12 characters");
    }
    if (value.length() > MAX_LENGTH) {
      violations.add("Password must be at most 64 characters");
    }
    if (value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
      violations.add("Password must be at most 72 bytes in UTF-8");
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
    if (value.toLowerCase().contains(email.value())) {
      violations.add("Password must not contain the email");
    }
    return violations;
  }

  @Override
  public String toString() {
    return "RawPassword[****]";
  }
}
