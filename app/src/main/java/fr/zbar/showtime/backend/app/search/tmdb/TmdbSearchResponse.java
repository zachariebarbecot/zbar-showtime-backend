package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

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
      if (mediaType == null) {
        throw new CatalogUnavailableException(new IllegalStateException("Missing media_type"));
      }
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

    CatalogItem toItem(MediaType type, TmdbProperties properties) {
      String date = date(type);
      return new CatalogItem(
          id,
          type,
          title(type),
          properties.imageUrl(properties.posterSize(), posterPath),
          StringUtils.isEmpty(date) ? null : LocalDate.parse(date));
    }

    String date(MediaType type) {
      return switch (type) {
        case MOVIE -> releaseDate;
        case TV -> firstAirDate;
      };
    }
  }
}
