package fr.zbar.showtime.backend.app.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record TmdbSearchResponse(
    int page,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_results") long totalResults,
    List<Movie> results) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Movie(
      long id,
      String title,
      @JsonProperty("poster_path") String posterPath,
      @JsonProperty("release_date") String releaseDate) {}
}
