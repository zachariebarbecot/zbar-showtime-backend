package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistEntryNotFoundException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class RemoveSeriesFromWatchlistService implements RemoveSeriesFromWatchlist {

  private final WatchlistSeriesRepository watchlistSeriesRepository;

  @Override
  public void execute(RemoveSeriesFromWatchlistCommand command) {
    WatchlistSeries series =
        watchlistSeriesRepository
            .findByUserIdAndTmdbId(command.userId(), command.tmdbId())
            .orElseThrow(WatchlistEntryNotFoundException::new);
    watchlistSeriesRepository.delete(series);
  }
}
