package fr.zbar.showtime.backend.core.watchlist.domain;

public enum SeriesStatus {
  TO_WATCH,
  WATCHING,
  WATCHED,
  DROPPED;

  public static SeriesStatus from(String value) {
    return valueOf(value);
  }
}
