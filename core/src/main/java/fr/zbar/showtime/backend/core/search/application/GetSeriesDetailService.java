package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.TmdbId;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetSeriesDetailService implements GetSeriesDetail {

  private final SeriesDetailCatalog seriesDetailCatalog;
  private final WatchlistSeriesRepository watchlistSeriesRepository;

  @Override
  public SeriesDetailView execute(GetSeriesDetailCommand command) {
    long tmdbId = new TmdbId(command.tmdbId()).value();
    String watchlistStatus =
        watchlistSeriesRepository
            .findByUserIdAndTmdbId(command.userId(), tmdbId)
            .map(series -> series.status().name())
            .orElse(null);
    return new SeriesDetailView(seriesDetailCatalog.findSeriesDetail(tmdbId), watchlistStatus);
  }
}
