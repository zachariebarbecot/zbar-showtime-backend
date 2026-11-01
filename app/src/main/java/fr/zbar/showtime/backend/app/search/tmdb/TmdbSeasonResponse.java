package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
record TmdbSeasonResponse(
    @JsonProperty("season_number") int seasonNumber,
    String name,
    String overview,
    @JsonProperty("air_date") String airDate,
    @JsonProperty("poster_path") String posterPath,
    List<TmdbEpisode> episodes) {

  TmdbSeasonResponse {
    episodes = episodes == null ? List.of() : episodes;
  }

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
          StringUtils.defaultIfEmpty(overview, null),
          runtime == null || runtime == 0 ? null : runtime,
          TmdbDates.parse(airDate),
          properties.imageUrl(properties.stillSize(), stillPath));
    }
  }

  SeasonDetail toSeasonDetail(TmdbProperties properties) {
    return new SeasonDetail(
        seasonNumber,
        name,
        StringUtils.defaultIfEmpty(overview, null),
        TmdbDates.parse(airDate),
        properties.imageUrl(properties.posterSize(), posterPath),
        episodes.stream().map(episode -> episode.toEpisode(properties)).toList());
  }
}
