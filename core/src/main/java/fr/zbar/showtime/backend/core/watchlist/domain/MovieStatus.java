package fr.zbar.showtime.backend.core.watchlist.domain;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;

public enum MovieStatus {
  TO_WATCH,
  WATCHED;

  public static MovieStatus from(String value) {
    for (MovieStatus status : values()) {
      if (status.name().equals(value)) {
        return status;
      }
    }
    throw new InvalidFieldException("status", "Status must be TO_WATCH or WATCHED");
  }
}
