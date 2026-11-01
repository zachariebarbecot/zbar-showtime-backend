package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Component
class TmdbTitleAdapter implements TitleCatalog {

  private final TmdbMovieApi movieApi;
  private final TmdbSeriesApi seriesApi;
  private final TmdbProperties properties;

  TmdbTitleAdapter(RestClient tmdbRestClient, TmdbProperties properties) {
    HttpServiceProxyFactory factory =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(tmdbRestClient)).build();
    this.movieApi = factory.createClient(TmdbMovieApi.class);
    this.seriesApi = factory.createClient(TmdbSeriesApi.class);
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
