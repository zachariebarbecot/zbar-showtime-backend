package fr.zbar.showtime.backend.core.domain;

import java.time.Clock;
import java.time.LocalDate;

public record BirthDate(LocalDate value) {

  public static BirthDate of(LocalDate value, Clock clock) {
    return new BirthDate(value);
  }

  public BirthDate(LocalDate value, Clock clock) {
    this(requirePast(value, clock));
  }

  private static LocalDate requirePast(LocalDate value, Clock clock) {
    if (value == null) {
      throw new InvalidFieldException("birthDate", "Birth date cannot be null");
    }
    if (!value.isBefore(LocalDate.now(clock))) {
      throw new InvalidFieldException("birthDate", "Birth date must be in the past");
    }
    return value;
  }

  @Override
  public String toString() {
    return "BirthDate[****]";
  }
}
