package fr.zbar.showtime.backend.app.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.domain.MediaType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
record TmdbSearchResponse(
    int page,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_results") long totalResults,
    List<Result> results) {

  TmdbSearchResponse {
    Objects.requireNonNull(results, "results");
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Result(
      long id,
      @JsonProperty("media_type") String mediaType,
      String title,
      String name,
      @JsonProperty("poster_path") String posterPath,
      @JsonProperty("release_date") String releaseDate,
      @JsonProperty("first_air_date") String firstAirDate) {

    Optional<MediaType> type() {
      return switch (mediaType) {
        case "movie" -> Optional.of(MediaType.MOVIE);
        case "tv" -> Optional.of(MediaType.TV);
        default -> Optional.empty();
      };
    }

    String title(MediaType type) {
      return switch (type) {
        case MOVIE -> title;
        case TV -> name;
      };
    }

    String date(MediaType type) {
      return switch (type) {
        case MOVIE -> releaseDate;
        case TV -> firstAirDate;
      };
    }
  }
}
