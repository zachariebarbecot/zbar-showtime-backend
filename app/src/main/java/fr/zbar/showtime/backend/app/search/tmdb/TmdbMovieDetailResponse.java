package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
record TmdbMovieDetailResponse(
    long id,
    String title,
    @JsonProperty("original_title") String originalTitle,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("release_date") String releaseDate,
    Integer runtime,
    String status,
    List<Genre> genres,
    String overview) {

  TmdbMovieDetailResponse {
    genres = genres == null ? List.of() : genres;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Genre(String name) {}

  MovieDetail toMovieDetail(TmdbProperties properties) {
    return new MovieDetail(
        id,
        title,
        originalTitle,
        genres.stream().map(Genre::name).toList(),
        StringUtils.isEmpty(releaseDate) ? null : LocalDate.parse(releaseDate),
        runtime,
        movieStatus(),
        overview,
        properties.imageUrl(properties.posterSize(), posterPath),
        List.of(),
        List.of(),
        List.of());
  }

  private MovieStatus movieStatus() {
    return switch (status) {
      case "Rumored" -> MovieStatus.RUMORED;
      case "Planned" -> MovieStatus.PLANNED;
      case "In Production" -> MovieStatus.IN_PRODUCTION;
      case "Post Production" -> MovieStatus.POST_PRODUCTION;
      case "Released" -> MovieStatus.RELEASED;
      case "Canceled" -> MovieStatus.CANCELED;
      case null, default -> null;
    };
  }
}
