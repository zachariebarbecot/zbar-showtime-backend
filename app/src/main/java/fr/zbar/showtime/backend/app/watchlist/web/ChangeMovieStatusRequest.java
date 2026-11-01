package fr.zbar.showtime.backend.app.watchlist.web;

import fr.zbar.showtime.backend.core.watchlist.application.ChangeMovieStatusCommand;
import fr.zbar.showtime.backend.core.watchlist.domain.MovieStatus;
import java.util.UUID;

public record ChangeMovieStatusRequest(String status) {

  ChangeMovieStatusCommand toCommand(UUID userId, long tmdbId) {
    return new ChangeMovieStatusCommand(userId, tmdbId, MovieStatus.from(status));
  }
}
