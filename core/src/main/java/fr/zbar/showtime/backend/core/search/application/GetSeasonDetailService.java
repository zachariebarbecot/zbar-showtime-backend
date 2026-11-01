package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import lombok.RequiredArgsConstructor;

@CoreUseCase
@RequiredArgsConstructor
public class GetSeasonDetailService implements GetSeasonDetail {

  private final SeasonCatalog seasonCatalog;

  @Override
  public SeasonDetail execute(GetSeasonDetailCommand command) {
    return seasonCatalog.findSeason(command.tmdbId(), command.seasonNumber());
  }
}
