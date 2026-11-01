package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
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
    return null;
  }
}
