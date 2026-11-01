package fr.zbar.showtime.backend.app.watchlist.web;

import fr.zbar.showtime.backend.core.watchlist.application.ChangeSeriesStatusCommand;
import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
import java.util.UUID;

public record ChangeSeriesStatusRequest(String status) {

  ChangeSeriesStatusCommand toCommand(UUID userId, long tmdbId) {
    return new ChangeSeriesStatusCommand(userId, tmdbId, SeriesStatus.from(status));
  }
}
