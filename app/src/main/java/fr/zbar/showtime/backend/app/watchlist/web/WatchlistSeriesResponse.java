package fr.zbar.showtime.backend.app.watchlist.web;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import java.time.Instant;

public record WatchlistSeriesResponse(
    long tmdbId, String status, Instant addedAt, Instant watchedAt) {

  static WatchlistSeriesResponse from(WatchlistSeries series) {
    return new WatchlistSeriesResponse(
        series.tmdbId(), series.status().name(), series.addedAt(), series.watchedAt());
  }
}
