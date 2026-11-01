package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record TmdbSeasonResponse(
    @JsonProperty("season_number") int seasonNumber,
    String name,
    String overview,
    @JsonProperty("air_date") String airDate,
    @JsonProperty("poster_path") String posterPath,
    List<TmdbEpisode> episodes) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  record TmdbEpisode(
      @JsonProperty("episode_number") int episodeNumber,
      String name,
      String overview,
      Integer runtime,
      @JsonProperty("air_date") String airDate,
      @JsonProperty("still_path") String stillPath) {

    Episode toEpisode(TmdbProperties properties) {
      return new Episode(
          episodeNumber,
          name,
          overview,
          runtime,
          LocalDate.parse(airDate),
          properties.imageUrl(properties.stillSize(), stillPath));
    }
  }

  SeasonDetail toSeasonDetail(TmdbProperties properties) {
    return new SeasonDetail(
        seasonNumber,
        name,
        overview,
        LocalDate.parse(airDate),
        properties.imageUrl(properties.posterSize(), posterPath),
        episodes.stream().map(episode -> episode.toEpisode(properties)).toList());
  }
}
