package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.PageNumber;
import fr.zbar.showtime.backend.core.search.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.search.domain.SearchPage;
import fr.zbar.showtime.backend.core.search.domain.SearchQuery;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
class TmdbSearchAdapter implements SearchCatalog {

  private final RestClient tmdbRestClient;
  private final TmdbProperties properties;

  @Override
  public SearchPage searchMovies(SearchQuery query, PageNumber page) {
    return search("/search/movie", result -> Optional.of(MediaType.MOVIE), query, page);
  }

  @Override
  public SearchPage searchSeries(SearchQuery query, PageNumber page) {
    return search("/search/tv", result -> Optional.of(MediaType.TV), query, page);
  }

  @Override
  public SearchPage searchAll(SearchQuery query, PageNumber page) {
    return search("/search/multi", TmdbSearchResponse.Result::type, query, page);
  }

  private SearchPage search(
      String path,
      Function<TmdbSearchResponse.Result, Optional<MediaType>> typeOf,
      SearchQuery query,
      PageNumber page) {
    try {
      return toPage(fetch(path, query, page), typeOf);
    } catch (RestClientException | DateTimeParseException exception) {
      throw new CatalogUnavailableException(exception);
    }
  }

  private TmdbSearchResponse fetch(String path, SearchQuery query, PageNumber page) {
    TmdbSearchResponse response =
        tmdbRestClient
            .get()
            .uri(
                uri ->
                    uri.path(path)
                        .queryParam("query", "{query}")
                        .queryParam("language", properties.language())
                        .queryParam("include_adult", false)
                        .queryParam("page", page.value())
                        .build(query.value()))
            .retrieve()
            .body(TmdbSearchResponse.class);
    if (response == null) {
      throw new CatalogUnavailableException(new IllegalStateException("Empty TMDB response body"));
    }
    return response;
  }

  private SearchPage toPage(
      TmdbSearchResponse response,
      Function<TmdbSearchResponse.Result, Optional<MediaType>> typeOf) {
    return new SearchPage(
        response.page(),
        response.totalPages(),
        response.totalResults(),
        response.results().stream()
            .flatMap(
                result ->
                    typeOf.apply(result).map(type -> result.toItem(type, properties)).stream())
            .toList());
  }
}
