package fr.zbar.showtime.backend.core.watchlist.domain;

public class WatchlistEntryNotFoundException extends RuntimeException {

  public WatchlistEntryNotFoundException() {
    super("Watchlist entry not found");
  }
}
