package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InMemoryWatchlistSeriesRepository implements WatchlistSeriesRepository {

  private final List<WatchlistSeries> series;

  public InMemoryWatchlistSeriesRepository(List<WatchlistSeries> series) {
    this.series = series;
  }

  @Override
  public void save(WatchlistSeries entry) {
    series.removeIf(existing -> existing.id().equals(entry.id()));
    series.add(entry);
  }

  @Override
  public void delete(WatchlistSeries entry) {}

  @Override
  public Optional<WatchlistSeries> findByUserIdAndTmdbId(UUID userId, long tmdbId) {
    return series.stream()
        .filter(entry -> entry.userId().equals(userId) && entry.tmdbId() == tmdbId)
        .findFirst();
  }

  @Override
  public List<WatchlistSeries> findByUserIdAndTmdbIdIn(UUID userId, Collection<Long> tmdbIds) {
    return series.stream()
        .filter(entry -> entry.userId().equals(userId) && tmdbIds.contains(entry.tmdbId()))
        .toList();
  }
}
