package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.util.Map;

public class InMemorySeasonCatalog implements SeasonCatalog {

  private final Map<String, SeasonDetail> seasons;

  public InMemorySeasonCatalog(long seriesId, SeasonDetail season) {
    this.seasons = Map.of(seriesId + "/" + season.seasonNumber(), season);
  }

  @Override
  public SeasonDetail findSeason(long seriesId, int seasonNumber) {
    SeasonDetail season = seasons.get(seriesId + "/" + seasonNumber);
    if (season == null) {
      throw new TitleNotFoundException();
    }
    return season;
  }
}
