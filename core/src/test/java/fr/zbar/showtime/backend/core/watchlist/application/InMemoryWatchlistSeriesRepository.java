package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import java.util.List;

public class InMemoryWatchlistSeriesRepository implements WatchlistSeriesRepository {

  private final List<WatchlistSeries> series;

  public InMemoryWatchlistSeriesRepository(List<WatchlistSeries> series) {
    this.series = series;
  }

  @Override
  public void save(WatchlistSeries entry) {
    series.add(entry);
  }
}
