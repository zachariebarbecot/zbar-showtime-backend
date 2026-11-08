package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
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
public class MarkEpisodeWatchedService implements MarkEpisodeWatched {

  private final WatchlistSeriesRepository watchlistSeriesRepository;
  private final SeasonCatalog seasonCatalog;
  private final WatchedEpisodeRepository watchedEpisodeRepository;
  private final Clock clock;

  @Override
  public void execute(MarkEpisodeWatchedCommand command) {
    WatchlistSeries series =
        watchlistSeriesRepository
            .findByUserIdAndTmdbId(command.userId(), command.tmdbId())
            .orElseThrow(WatchlistEntryNotFoundException::new);
    if (!existsInCatalog(command)) {
      throw new TitleNotFoundException();
    }
    if (watchedEpisodeRepository.existsByUserIdAndSeriesTmdbIdAndSeasonNumberAndEpisodeNumber(
        command.userId(), command.tmdbId(), command.seasonNumber(), command.episodeNumber())) {
      return;
    }
    watchedEpisodeRepository.save(
        WatchedEpisode.mark(
            command.userId(),
            command.tmdbId(),
            command.seasonNumber(),
            command.episodeNumber(),
            clock));
    watchlistSeriesRepository.save(series.onEpisodeWatched(clock));
  }

  private boolean existsInCatalog(MarkEpisodeWatchedCommand command) {
    return seasonCatalog.findSeason(command.tmdbId(), command.seasonNumber()).episodes().stream()
        .anyMatch(episode -> episode.episodeNumber() == command.episodeNumber());
  }
}
