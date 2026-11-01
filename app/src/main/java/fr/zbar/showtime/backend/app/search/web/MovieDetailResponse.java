package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import java.time.LocalDate;
import java.util.List;

public record MovieDetailResponse(
    long id,
    String title,
    String originalTitle,
    String posterUrl,
    LocalDate releaseDate,
    Integer runtime,
    List<String> genres,
    String overview,
    MovieStatus status) {

  static MovieDetailResponse from(MovieDetail movie) {
    return new MovieDetailResponse(
        movie.id(),
        movie.title(),
        movie.originalTitle(),
        movie.posterUrl(),
        movie.releaseDate(),
        movie.runtime(),
        movie.genres(),
        movie.overview(),
        movie.status());
  }
}
