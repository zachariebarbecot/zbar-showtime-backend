package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import org.springframework.stereotype.Component;

@Component
class TmdbSeasonAdapter implements SeasonCatalog {

  private final TmdbSeriesApi seriesApi;
  private final TmdbProperties properties;

  TmdbSeasonAdapter(TmdbSeriesApi seriesApi, TmdbProperties properties) {
    this.seriesApi = seriesApi;
    this.properties = properties;
  }

  @Override
  public SeasonDetail findSeason(long seriesId, int seasonNumber) {
    return TmdbCalls.fetch(
        () -> seriesApi.seasonDetail(seriesId, seasonNumber, properties.language()),
        response -> response.toSeasonDetail(properties));
  }
}
