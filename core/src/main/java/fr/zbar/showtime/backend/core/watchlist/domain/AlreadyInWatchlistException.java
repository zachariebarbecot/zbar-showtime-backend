package fr.zbar.showtime.backend.core.watchlist.domain;

public class AlreadyInWatchlistException extends RuntimeException {

  public AlreadyInWatchlistException() {
    super("Title already in watchlist");
  }
}
