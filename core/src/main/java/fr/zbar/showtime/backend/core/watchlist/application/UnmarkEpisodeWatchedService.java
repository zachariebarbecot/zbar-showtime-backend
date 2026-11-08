package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistEntryNotFoundException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class UnmarkEpisodeWatchedService implements UnmarkEpisodeWatched {

  private final WatchedEpisodeRepository watchedEpisodeRepository;
  private final WatchlistSeriesRepository watchlistSeriesRepository;
  private final Clock clock;

  @Override
  public void execute(UnmarkEpisodeWatchedCommand command) {
    WatchedEpisode episode =
        watchedEpisodeRepository
            .findByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
                command.userId(), command.tmdbId(), command.seasonNumber(), command.episodeNumber())
            .orElseThrow(WatchlistEntryNotFoundException::new);
    WatchlistSeries series =
        watchlistSeriesRepository
            .findByUserIdAndTmdbId(command.userId(), command.tmdbId())
            .orElseThrow(WatchlistEntryNotFoundException::new);
    watchedEpisodeRepository.delete(episode);
    WatchlistSeries updated =
        command.seasonNumber() > 0 ? series.onRegularEpisodeUnwatched(clock) : series;
    if (!watchedEpisodeRepository.existsByUserIdAndSeriesTmdbId(
        command.userId(), command.tmdbId())) {
      updated = updated.onLastEpisodeUnwatched(clock);
    }
    watchlistSeriesRepository.save(updated);
  }
}
