package fr.zbar.showtime.backend.core.watchlist.domain;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;

public enum SeriesStatus {
  TO_WATCH,
  WATCHING,
  WATCHED,
  DROPPED;

  public static SeriesStatus from(String value) {
    for (SeriesStatus status : values()) {
      if (status.name().equals(value)) {
        return status;
      }
    }
    throw new InvalidFieldException(
        "status", "Status must be TO_WATCH, WATCHING, WATCHED or DROPPED");
  }
}
