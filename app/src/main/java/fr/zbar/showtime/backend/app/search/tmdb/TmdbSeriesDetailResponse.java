package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    String overview) {}
