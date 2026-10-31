package fr.zbar.showtime.backend.core.domain;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;

public record PageNumber(int value) {

  private static final int MIN = 1;
  private static final int MAX = 500;

  public PageNumber {
    if (value < MIN || value > MAX) {
      throw new InvalidFieldException("page", "Page must be between %d and %d".formatted(MIN, MAX));
    }
  }
}
