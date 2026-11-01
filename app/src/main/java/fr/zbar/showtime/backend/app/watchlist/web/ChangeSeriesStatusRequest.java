package fr.zbar.showtime.backend.app.watchlist.web;

import fr.zbar.showtime.backend.core.watchlist.application.ChangeSeriesStatusCommand;
import java.util.UUID;

public record ChangeSeriesStatusRequest(String status) {

  ChangeSeriesStatusCommand toCommand(UUID userId, long tmdbId) {
    return null;
  }
}
