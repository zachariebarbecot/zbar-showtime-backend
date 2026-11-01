package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Component
class TmdbTitleAdapter implements TitleCatalog {

  private final TmdbMovieApi movieApi;
  private final TmdbProperties properties;

  TmdbTitleAdapter(RestClient tmdbRestClient, TmdbProperties properties) {
    this.movieApi =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(tmdbRestClient))
            .build()
            .createClient(TmdbMovieApi.class);
    this.properties = properties;
  }

  @Override
  public CatalogItem findMovie(long id) {
    try {
      return fetchMovie(id).toItem(MediaType.MOVIE, properties);
    } catch (HttpClientErrorException.NotFound exception) {
      throw new TitleNotFoundException();
    } catch (RestClientException | DateTimeParseException exception) {
      throw new CatalogUnavailableException(exception);
    }
  }

  private TmdbSearchResponse.Result fetchMovie(long id) {
    TmdbSearchResponse.Result result = movieApi.movie(id, properties.language());
    if (result == null) {
      throw new CatalogUnavailableException(new IllegalStateException("Empty TMDB response body"));
    }
    return result;
  }
}
