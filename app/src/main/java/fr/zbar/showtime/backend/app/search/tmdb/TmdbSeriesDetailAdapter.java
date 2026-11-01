package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import org.springframework.stereotype.Component;

@Component
class TmdbSeriesDetailAdapter implements SeriesDetailCatalog {

  private final TmdbSeriesApi seriesApi;
  private final TmdbProperties properties;

  TmdbSeriesDetailAdapter(TmdbSeriesApi seriesApi, TmdbProperties properties) {
    this.seriesApi = seriesApi;
    this.properties = properties;
  }

  @Override
  public SeriesDetail findSeriesDetail(long id) {
    return null;
  }
}
