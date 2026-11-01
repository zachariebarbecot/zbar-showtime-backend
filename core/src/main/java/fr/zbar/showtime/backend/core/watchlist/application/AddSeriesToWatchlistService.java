package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class AddSeriesToWatchlistService implements AddSeriesToWatchlist {

  private final TitleCatalog titleCatalog;
  private final WatchlistSeriesRepository watchlistSeriesRepository;
  private final Clock clock;

  @Override
  public void execute(AddSeriesToWatchlistCommand command) {
    titleCatalog.findSeries(command.tmdbId());
    watchlistSeriesRepository.save(WatchlistSeries.add(command.userId(), command.tmdbId(), clock));
  }
}
