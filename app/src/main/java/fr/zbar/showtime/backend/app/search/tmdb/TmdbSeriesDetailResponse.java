package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.CastMember;
import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

  private static final int MAX_CAST = 15;
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
  record Cast(String name, List<Role> roles, @JsonProperty("profile_path") String profilePath) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Role(String character) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Crew(long id, String name, List<Job> jobs) {}

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
        keyCrew(),
        cast(properties));
  }

  private List<CastMember> cast(TmdbProperties properties) {
    return aggregateCredits.cast().stream()
        .limit(MAX_CAST)
        .map(
            actor ->
                new CastMember(
                    actor.name(),
                    actor.roles().getFirst().character(),
                    properties.imageUrl(properties.profileSize(), actor.profilePath())))
        .toList();
  }

  private List<CrewMember> keyCrew() {
    return aggregateCredits.crew().stream()
        .flatMap(
            member ->
                member.jobs().stream()
                    .filter(job -> KEY_JOBS.contains(job.job()))
                    .map(job -> new KeyJob(member.id(), member.name(), job.job())))
        .collect(Collectors.groupingBy(KeyJob::id, LinkedHashMap::new, Collectors.toList()))
        .values()
        .stream()
        .map(
            jobs ->
                new CrewMember(
                    jobs.getFirst().name(), jobs.stream().map(KeyJob::job).distinct().toList()))
        .toList();
  }

  private record KeyJob(long id, String name, String job) {}

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
