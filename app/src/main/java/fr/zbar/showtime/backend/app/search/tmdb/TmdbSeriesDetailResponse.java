package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record TmdbSeriesDetailResponse(
    long id,
    String name,
    @JsonProperty("original_name") String originalName,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("first_air_date") String firstAirDate,
    @JsonProperty("last_air_date") String lastAirDate,
    @JsonProperty("number_of_seasons") int numberOfSeasons,
    @JsonProperty("number_of_episodes") int numberOfEpisodes,
    String status,
    List<Genre> genres,
    String overview) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Genre(String name) {}

  SeriesDetail toSeriesDetail(TmdbProperties properties) {
    return new SeriesDetail(
        id,
        name,
        originalName,
        genres.stream().map(Genre::name).toList(),
        LocalDate.parse(firstAirDate),
        LocalDate.parse(lastAirDate),
        seriesStatus(),
        overview,
        properties.imageUrl(properties.posterSize(), posterPath),
        numberOfSeasons,
        numberOfEpisodes,
        List.of(),
        List.of(),
        List.of());
  }

  private SeriesStatus seriesStatus() {
    return switch (status) {
      case "Returning Series" -> SeriesStatus.RETURNING;
      case "Ended" -> SeriesStatus.ENDED;
      case "Canceled" -> SeriesStatus.CANCELED;
      case "In Production" -> SeriesStatus.IN_PRODUCTION;
      case "Planned" -> SeriesStatus.PLANNED;
      case "Pilot" -> SeriesStatus.PILOT;
      default -> null;
    };
  }
}
