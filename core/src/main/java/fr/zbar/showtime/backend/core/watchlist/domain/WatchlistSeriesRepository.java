package fr.zbar.showtime.backend.core.watchlist.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchlistSeriesRepository {

  void save(WatchlistSeries series);

  Optional<WatchlistSeries> findByUserIdAndTmdbId(UUID userId, long tmdbId);

  List<WatchlistSeries> findByUserIdAndTmdbIdIn(UUID userId, Collection<Long> tmdbIds);
}
