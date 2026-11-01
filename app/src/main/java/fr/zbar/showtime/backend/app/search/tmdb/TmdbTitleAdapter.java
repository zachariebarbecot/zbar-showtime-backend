package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
class TmdbTitleAdapter implements TitleCatalog {

  private final RestClient tmdbRestClient;
  private final TmdbProperties properties;

  @Override
  public CatalogItem findMovie(long id) {
    return tmdbRestClient
        .get()
        .uri(uri -> uri.path("/movie/{id}").queryParam("language", properties.language()).build(id))
        .retrieve()
        .body(TmdbSearchResponse.Result.class)
        .toItem(MediaType.MOVIE, properties);
  }
}
