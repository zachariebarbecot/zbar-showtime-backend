package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
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
  private final SeriesDetailCatalog seriesDetailCatalog;
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
    WatchlistSeries started = series.onEpisodeWatched(clock);
    watchlistSeriesRepository.save(
        isCompleted(command) ? started.changeStatus(SeriesStatus.WATCHED, clock) : started);
  }

  private boolean isCompleted(MarkEpisodeWatchedCommand command) {
    SeriesDetail detail = seriesDetailCatalog.findSeriesDetail(command.tmdbId());
    int total = detail.totalEpisodes();
    return (detail.status() == fr.zbar.showtime.backend.core.search.domain.SeriesStatus.ENDED
            || detail.status() == fr.zbar.showtime.backend.core.search.domain.SeriesStatus.CANCELED)
        && total > 0
        && watchedEpisodeRepository.countByUserIdAndSeriesTmdbIdExcludingSpecials(
                command.userId(), command.tmdbId())
            >= total;
  }

  private boolean existsInCatalog(MarkEpisodeWatchedCommand command) {
    return seasonCatalog.findSeason(command.tmdbId(), command.seasonNumber()).episodes().stream()
        .anyMatch(episode -> episode.episodeNumber() == command.episodeNumber());
  }
}
