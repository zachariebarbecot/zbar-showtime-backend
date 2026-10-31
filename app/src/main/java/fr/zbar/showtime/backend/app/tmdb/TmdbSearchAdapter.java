package fr.zbar.showtime.backend.app.tmdb;

import fr.zbar.showtime.backend.core.domain.CatalogItem;
import fr.zbar.showtime.backend.core.domain.MediaType;
import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
class TmdbSearchAdapter implements SearchCatalog {

  private final RestClient tmdbRestClient;
  private final TmdbProperties properties;

  @Override
  public SearchPage searchMovies(SearchQuery query, PageNumber page) {
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
        properties.imageBaseUrl() + "/" + properties.posterSize() + movie.posterPath(),
        LocalDate.parse(movie.releaseDate()));
  }
}
