package fr.zbar.showtime.backend.core.watchlist.domain;

public enum MovieStatus {
  TO_WATCH,
  WATCHED;

  public static MovieStatus from(String value) {
    return valueOf(value);
  }
}
