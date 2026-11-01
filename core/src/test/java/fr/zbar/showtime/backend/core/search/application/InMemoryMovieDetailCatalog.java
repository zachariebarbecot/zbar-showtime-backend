package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.util.List;

public class InMemoryMovieDetailCatalog implements MovieDetailCatalog {

  private final List<MovieDetail> movies;

  public InMemoryMovieDetailCatalog(MovieDetail... movies) {
    this.movies = List.of(movies);
  }

  @Override
  public MovieDetail findMovieDetail(long id) {
    return movies.stream()
        .filter(movie -> movie.id() == id)
        .findFirst()
        .orElseThrow(TitleNotFoundException::new);
  }
}
