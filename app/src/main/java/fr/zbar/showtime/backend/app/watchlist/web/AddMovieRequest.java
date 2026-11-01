package fr.zbar.showtime.backend.app.watchlist.web;

import fr.zbar.showtime.backend.core.watchlist.application.AddMovieToWatchlistCommand;
import java.util.UUID;

public record AddMovieRequest(long tmdbId) {

  AddMovieToWatchlistCommand toCommand(UUID userId) {
    return new AddMovieToWatchlistCommand(userId, tmdbId);
  }
}
