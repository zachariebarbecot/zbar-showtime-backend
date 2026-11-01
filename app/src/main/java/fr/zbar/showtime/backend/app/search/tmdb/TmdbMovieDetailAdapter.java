package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieDetailCatalog;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class TmdbMovieDetailAdapter implements MovieDetailCatalog {

  TmdbMovieDetailAdapter(RestClient tmdbRestClient, TmdbProperties properties) {}

  @Override
  public MovieDetail findMovieDetail(long id) {
    return null;
  }
}
