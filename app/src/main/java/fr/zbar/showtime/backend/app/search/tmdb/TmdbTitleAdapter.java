package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
class TmdbTitleAdapter implements TitleCatalog {

  private final TmdbMovieApi movieApi;
  private final TmdbSeriesApi seriesApi;
  private final TmdbProperties properties;

  TmdbTitleAdapter(TmdbMovieApi movieApi, TmdbSeriesApi seriesApi, TmdbProperties properties) {
    this.movieApi = movieApi;
    this.seriesApi = seriesApi;
    this.properties = properties;
  }

  @Override
  public CatalogItem findMovie(long id) {
    return find(MediaType.MOVIE, () -> movieApi.movie(id, properties.language()));
  }

  @Override
  public CatalogItem findSeries(long id) {
    return find(MediaType.TV, () -> seriesApi.series(id, properties.language()));
  }

  private CatalogItem find(MediaType type, Supplier<TmdbSearchResponse.Result> fetch) {
    return TmdbCalls.fetch(fetch, result -> result.toItem(type, properties));
  }
}
