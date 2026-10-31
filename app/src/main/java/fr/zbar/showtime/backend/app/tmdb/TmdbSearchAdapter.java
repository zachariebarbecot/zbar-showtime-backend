package fr.zbar.showtime.backend.app.tmdb;

import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import org.springframework.web.client.RestClient;

class TmdbSearchAdapter implements SearchCatalog {

  TmdbSearchAdapter(RestClient restClient, TmdbProperties properties) {}

  @Override
  public SearchPage searchMovies(SearchQuery query, PageNumber page) {
    return null;
  }
}
