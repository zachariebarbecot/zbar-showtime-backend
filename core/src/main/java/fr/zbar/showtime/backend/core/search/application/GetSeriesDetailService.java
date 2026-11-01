package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetSeriesDetailService implements GetSeriesDetail {

  private final SeriesDetailCatalog seriesDetailCatalog;

  @Override
  public SeriesDetail execute(GetSeriesDetailCommand command) {
    if (command.tmdbId() <= 0) {
      throw new InvalidFieldException("tmdbId", "TMDB id must be greater than 0");
    }
    return seriesDetailCatalog.findSeriesDetail(command.tmdbId());
  }
}
