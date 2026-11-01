package fr.zbar.showtime.backend.app.watchlist.web;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import java.time.Instant;

public record WatchlistMovieResponse(
    long tmdbId, String status, Instant addedAt, Instant watchedAt) {

  static WatchlistMovieResponse from(WatchlistMovie movie) {
    return new WatchlistMovieResponse(
        movie.tmdbId(), movie.status().name(), movie.addedAt(), movie.watchedAt());
  }
}
