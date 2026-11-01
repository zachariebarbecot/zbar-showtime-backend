package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistEntryNotFoundException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class ChangeSeriesStatusService implements ChangeSeriesStatus {

  private final WatchlistSeriesRepository watchlistSeriesRepository;
  private final Clock clock;

  @Override
  public WatchlistSeries execute(ChangeSeriesStatusCommand command) {
    WatchlistSeries series =
        watchlistSeriesRepository
            .findByUserIdAndTmdbId(command.userId(), command.tmdbId())
            .orElseThrow(WatchlistEntryNotFoundException::new);
    WatchlistSeries changed = series.changeStatus(command.status(), clock);
    watchlistSeriesRepository.save(changed);
    return changed;
  }
}
