package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetSeasonDetailService implements GetSeasonDetail {

  private final SeasonCatalog seasonCatalog;

  @Override
  public SeasonDetail execute(GetSeasonDetailCommand command) {
    if (command.tmdbId() <= 0) {
      throw new InvalidFieldException("tmdbId", "TMDB id must be greater than 0");
    }
    if (command.seasonNumber() < 0) {
      throw new InvalidFieldException("seasonNumber", "Season number must not be negative");
    }
    return seasonCatalog.findSeason(command.tmdbId(), command.seasonNumber());
  }
}
