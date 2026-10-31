package fr.zbar.showtime.backend.core.domain;

import java.time.Clock;
import java.time.LocalDate;

/**
 * A birth date. The canonical constructor does not validate: it only rehydrates a value already
 * stored. Use {@link #of(LocalDate, Clock)} for external input.
 */
public record BirthDate(LocalDate value) {

  private static final LocalDate EARLIEST = LocalDate.of(1900, 1, 1);

  public static BirthDate of(LocalDate value, Clock clock) {
    if (value == null) {
      throw new InvalidFieldException("birthDate", "Birth date cannot be null");
    }
    if (!value.isBefore(LocalDate.now(clock))) {
      throw new InvalidFieldException("birthDate", "Birth date must be in the past");
    }
    if (value.isBefore(EARLIEST)) {
      throw new InvalidFieldException("birthDate", "Birth date must be on or after " + EARLIEST);
    }
    return new BirthDate(value);
  }

  @Override
  public String toString() {
    return "BirthDate[****]";
  }
}
