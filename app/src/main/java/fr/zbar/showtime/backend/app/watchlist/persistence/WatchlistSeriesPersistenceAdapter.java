package fr.zbar.showtime.backend.app.watchlist.persistence;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class WatchlistSeriesPersistenceAdapter implements WatchlistSeriesRepository {

  private final WatchlistSeriesJpaRepository watchlistSeriesJpaRepository;

  @Override
  public void save(WatchlistSeries series) {
    watchlistSeriesJpaRepository.saveAndFlush(
        new WatchlistSeriesJpaEntity(
            series.id(),
            series.userId(),
            series.tmdbId(),
            series.status().name(),
            series.addedAt(),
            series.watchedAt()));
  }
}
