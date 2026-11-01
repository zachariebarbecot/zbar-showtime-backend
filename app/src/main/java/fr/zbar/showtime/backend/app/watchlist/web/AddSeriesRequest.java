package fr.zbar.showtime.backend.app.watchlist.web;

import fr.zbar.showtime.backend.core.watchlist.application.AddSeriesToWatchlistCommand;
import java.util.UUID;

public record AddSeriesRequest(long tmdbId) {

  AddSeriesToWatchlistCommand toCommand(UUID userId) {
    return new AddSeriesToWatchlistCommand(userId, tmdbId);
  }
}
