package fr.zbar.showtime.backend.core.domain;

import java.time.Clock;
import java.time.LocalDate;

public record BirthDate(LocalDate value) {

  public BirthDate(LocalDate value, Clock clock) {
    this(value);
  }
}
