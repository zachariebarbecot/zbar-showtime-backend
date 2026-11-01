package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class RemoveSeriesFromWatchlistService implements RemoveSeriesFromWatchlist {

  private final WatchlistSeriesRepository watchlistSeriesRepository;

  @Override
  public void execute(RemoveSeriesFromWatchlistCommand command) {
    watchlistSeriesRepository
        .findByUserIdAndTmdbId(command.userId(), command.tmdbId())
        .ifPresent(watchlistSeriesRepository::delete);
  }
}
