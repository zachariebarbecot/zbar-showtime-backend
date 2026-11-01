package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieDetailCatalog;
import org.springframework.stereotype.Component;

@Component
class TmdbMovieDetailAdapter implements MovieDetailCatalog {

  private final TmdbMovieApi movieApi;
  private final TmdbProperties properties;

  TmdbMovieDetailAdapter(TmdbMovieApi movieApi, TmdbProperties properties) {
    this.movieApi = movieApi;
    this.properties = properties;
  }

  @Override
  public MovieDetail findMovieDetail(long id) {
    return TmdbCalls.fetch(
        () -> movieApi.movieDetail(id, properties.language()),
        response -> response.toMovieDetail(properties));
  }
}
