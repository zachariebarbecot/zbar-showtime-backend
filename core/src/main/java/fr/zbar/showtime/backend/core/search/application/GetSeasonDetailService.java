package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.search.domain.TmdbId;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetSeasonDetailService implements GetSeasonDetail {

  private final SeasonCatalog seasonCatalog;

  @Override
  public SeasonDetail execute(GetSeasonDetailCommand command) {
    long tmdbId = new TmdbId(command.tmdbId()).value();
    if (command.seasonNumber() < 0) {
      throw new InvalidFieldException("seasonNumber", "Season number must not be negative");
    }
    return seasonCatalog.findSeason(tmdbId, command.seasonNumber());
  }
}
