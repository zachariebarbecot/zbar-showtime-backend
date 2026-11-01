package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieDetailCatalog;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Component
class TmdbMovieDetailAdapter implements MovieDetailCatalog {

  private final TmdbMovieApi movieApi;
  private final TmdbProperties properties;

  TmdbMovieDetailAdapter(RestClient tmdbRestClient, TmdbProperties properties) {
    this.movieApi =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(tmdbRestClient))
            .build()
            .createClient(TmdbMovieApi.class);
    this.properties = properties;
  }

  @Override
  public MovieDetail findMovieDetail(long id) {
    return TmdbCalls.fetch(
        () -> movieApi.movieDetail(id, properties.language()),
        response -> response.toMovieDetail(properties));
  }
}
