package fr.zbar.showtime.backend.core.domain;

import org.apache.commons.lang3.StringUtils;

public record FullName(String lastName, String firstName) {

  private static final int MAX_LENGTH = 100;

  public FullName {
    lastName = StringUtils.strip(lastName);
    firstName = StringUtils.strip(firstName);
    if (StringUtils.isBlank(lastName)) {
      throw new InvalidFieldException("lastName", "Last name cannot be blank");
    }
    if (StringUtils.isBlank(firstName)) {
      throw new InvalidFieldException("firstName", "First name cannot be blank");
    }
    if (lastName.length() > MAX_LENGTH) {
      throw new InvalidFieldException(
          "lastName", "Last name must be at most %d characters".formatted(MAX_LENGTH));
    }
    if (firstName.length() > MAX_LENGTH) {
      throw new InvalidFieldException(
          "firstName", "First name must be at most %d characters".formatted(MAX_LENGTH));
    }
  }

  @Override
  public String toString() {
    return "FullName[****]";
  }
}
