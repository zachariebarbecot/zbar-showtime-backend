package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.TmdbId;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisodeRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetSeriesDetailService implements GetSeriesDetail {

  private final SeriesDetailCatalog seriesDetailCatalog;
  private final WatchlistSeriesRepository watchlistSeriesRepository;
  private final WatchedEpisodeRepository watchedEpisodeRepository;

  @Override
  public SeriesDetailView execute(GetSeriesDetailCommand command) {
    long tmdbId = new TmdbId(command.tmdbId()).value();
    SeriesDetail series = seriesDetailCatalog.findSeriesDetail(tmdbId);
    return watchlistSeriesRepository
        .findByUserIdAndTmdbId(command.userId(), tmdbId)
        .map(
            entry ->
                new SeriesDetailView(
                    series,
                    entry.status().name(),
                    Math.toIntExact(
                        watchedEpisodeRepository.countByUserIdAndSeriesTmdbIdExcludingSpecials(
                            command.userId(), tmdbId)),
                    series.totalEpisodes()))
        .orElseGet(() -> new SeriesDetailView(series, null, null, null));
  }
}
