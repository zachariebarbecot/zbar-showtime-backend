package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
record TmdbSeriesDetailResponse(
    long id,
    String name,
    @JsonProperty("original_name") String originalName,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("first_air_date") String firstAirDate,
    @JsonProperty("last_air_date") String lastAirDate,
    @JsonProperty("number_of_seasons") Integer numberOfSeasons,
    @JsonProperty("number_of_episodes") Integer numberOfEpisodes,
    String status,
    List<Genre> genres,
    String overview,
    @JsonProperty("created_by") List<Creator> createdBy,
    @JsonProperty("aggregate_credits") Credits aggregateCredits) {

  private static final Set<String> KEY_JOBS =
      Set.of("Writer", "Screenplay", "Producer", "Composer");

  TmdbSeriesDetailResponse {
    genres = genres == null ? List.of() : genres;
    createdBy = createdBy == null ? List.of() : createdBy;
    aggregateCredits =
        aggregateCredits == null ? new Credits(List.of(), List.of()) : aggregateCredits;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Genre(String name) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Creator(String name) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Credits(List<Cast> cast, List<Crew> crew) {

    Credits {
      cast = cast == null ? List.of() : cast;
      crew = crew == null ? List.of() : crew;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Cast(String name, List<Role> roles, @JsonProperty("profile_path") String profilePath) {

    Cast {
      roles = roles == null ? List.of() : roles;
    }

    String character() {
      return roles.isEmpty() ? null : roles.getFirst().character();
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Role(String character) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Crew(long id, String name, List<Job> jobs) {

    Crew {
      jobs = jobs == null ? List.of() : jobs;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Job(String job) {}

  SeriesDetail toSeriesDetail(TmdbProperties properties) {
    return new SeriesDetail(
        id,
        name,
        originalName,
        genres.stream().map(Genre::name).toList(),
        date(firstAirDate),
        date(lastAirDate),
        seriesStatus(),
        StringUtils.defaultIfEmpty(overview, null),
        properties.imageUrl(properties.posterSize(), posterPath),
        numberOfSeasons,
        numberOfEpisodes,
        createdBy.stream().map(Creator::name).toList(),
        TmdbCredits.keyCrew(contributions(), KEY_JOBS),
        TmdbCredits.cast(actors(), properties));
  }

  private List<TmdbCredits.Contribution> contributions() {
    return aggregateCredits.crew().stream()
        .flatMap(
            member ->
                member.jobs().stream()
                    .map(
                        job -> new TmdbCredits.Contribution(member.id(), member.name(), job.job())))
        .toList();
  }

  private List<TmdbCredits.Actor> actors() {
    return aggregateCredits.cast().stream()
        .map(actor -> new TmdbCredits.Actor(actor.name(), actor.character(), actor.profilePath()))
        .toList();
  }

  private static LocalDate date(String value) {
    return StringUtils.isEmpty(value) ? null : LocalDate.parse(value);
  }

  private SeriesStatus seriesStatus() {
    return switch (status) {
      case "Returning Series" -> SeriesStatus.RETURNING;
      case "Ended" -> SeriesStatus.ENDED;
      case "Canceled" -> SeriesStatus.CANCELED;
      case "In Production" -> SeriesStatus.IN_PRODUCTION;
      case "Planned" -> SeriesStatus.PLANNED;
      case "Pilot" -> SeriesStatus.PILOT;
      case null, default -> null;
    };
  }
}
