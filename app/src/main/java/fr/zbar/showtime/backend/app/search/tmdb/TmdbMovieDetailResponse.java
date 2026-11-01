package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import java.util.List;
import java.util.Set;
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
    String overview,
    Credits credits) {

  private static final String DIRECTOR = "Director";
  private static final Set<String> KEY_JOBS =
      Set.of(DIRECTOR, "Writer", "Screenplay", "Producer", "Composer");

  TmdbMovieDetailResponse {
    genres = genres == null ? List.of() : genres;
    credits = credits == null ? new Credits(List.of(), List.of()) : credits;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Genre(String name) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Credits(List<TmdbCredits.Actor> cast, List<TmdbCredits.Contribution> crew) {

    Credits {
      cast = cast == null ? List.of() : cast;
      crew = crew == null ? List.of() : crew;
    }
  }

  MovieDetail toMovieDetail(TmdbProperties properties) {
    return new MovieDetail(
        id,
        title,
        originalTitle,
        genres.stream().map(Genre::name).toList(),
        TmdbDates.parse(releaseDate),
        runtime == null || runtime == 0 ? null : runtime,
        movieStatus(),
        StringUtils.defaultIfEmpty(overview, null),
        properties.imageUrl(properties.posterSize(), posterPath),
        directors(),
        TmdbCredits.keyCrew(credits.crew(), KEY_JOBS),
        TmdbCredits.cast(credits.cast(), properties));
  }

  private List<String> directors() {
    return credits.crew().stream()
        .filter(member -> DIRECTOR.equals(member.job()))
        .map(TmdbCredits.Contribution::name)
        .distinct()
        .toList();
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
