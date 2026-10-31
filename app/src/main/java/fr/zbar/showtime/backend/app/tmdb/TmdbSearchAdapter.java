package fr.zbar.showtime.backend.app.tmdb;

import fr.zbar.showtime.backend.core.domain.CatalogItem;
import fr.zbar.showtime.backend.core.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.domain.MediaType;
import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
    try {
      return toPage(fetch(query, page));
    } catch (RestClientException | DateTimeParseException exception) {
      throw new CatalogUnavailableException(exception);
    }
  }

  @Override
  public SearchPage searchSeries(SearchQuery query, PageNumber page) {
    throw new UnsupportedOperationException();
  }

  private TmdbSearchResponse fetch(SearchQuery query, PageNumber page) {
    TmdbSearchResponse response =
        tmdbRestClient
            .get()
            .uri(
                uri ->
                    uri.path("/search/movie")
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

  private SearchPage toPage(TmdbSearchResponse response) {
    return new SearchPage(
        response.page(),
        response.totalPages(),
        response.totalResults(),
        response.results().stream().map(this::toItem).toList());
  }

  private CatalogItem toItem(TmdbSearchResponse.Movie movie) {
    return new CatalogItem(
        movie.id(),
        MediaType.MOVIE,
        movie.title(),
        posterUrl(movie.posterPath()),
        StringUtils.isEmpty(movie.releaseDate()) ? null : LocalDate.parse(movie.releaseDate()));
  }

  private String posterUrl(String posterPath) {
    return StringUtils.isEmpty(posterPath)
        ? null
        : properties.imageBaseUrl() + "/" + properties.posterSize() + posterPath;
  }
}
