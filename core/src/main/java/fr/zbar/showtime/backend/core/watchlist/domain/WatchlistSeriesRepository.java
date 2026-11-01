package fr.zbar.showtime.backend.core.watchlist.domain;

import java.util.Optional;
import java.util.UUID;

public interface WatchlistSeriesRepository {

  void save(WatchlistSeries series);

  Optional<WatchlistSeries> findByUserIdAndTmdbId(UUID userId, long tmdbId);
}
