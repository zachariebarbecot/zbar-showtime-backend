package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class MarkEpisodeWatchedService implements MarkEpisodeWatched {

  private final WatchlistSeriesRepository watchlistSeriesRepository;
  private final SeasonCatalog seasonCatalog;
  private final WatchedEpisodeRepository watchedEpisodeRepository;
  private final Clock clock;

  @Override
  public void execute(MarkEpisodeWatchedCommand command) {}
}
