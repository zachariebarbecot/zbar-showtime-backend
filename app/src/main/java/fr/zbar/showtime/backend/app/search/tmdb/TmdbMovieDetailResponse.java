package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.CastMember;
import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

  private static final int MAX_CAST = 15;
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
  record Credits(List<Cast> cast, List<Crew> crew) {

    Credits {
      cast = cast == null ? List.of() : cast;
      crew = crew == null ? List.of() : crew;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Cast(String name, String character, @JsonProperty("profile_path") String profilePath) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Crew(long id, String name, String job) {}

  MovieDetail toMovieDetail(TmdbProperties properties) {
    return new MovieDetail(
        id,
        title,
        originalTitle,
        genres.stream().map(Genre::name).toList(),
        StringUtils.isEmpty(releaseDate) ? null : LocalDate.parse(releaseDate),
        runtime == null || runtime == 0 ? null : runtime,
        movieStatus(),
        StringUtils.defaultIfEmpty(overview, null),
        properties.imageUrl(properties.posterSize(), posterPath),
        directors(),
        keyCrew(),
        cast(properties));
  }

  private List<CastMember> cast(TmdbProperties properties) {
    return credits.cast().stream()
        .limit(MAX_CAST)
        .map(
            actor ->
                new CastMember(
                    actor.name(),
                    actor.character(),
                    properties.imageUrl(properties.profileSize(), actor.profilePath())))
        .toList();
  }

  private List<String> directors() {
    return credits.crew().stream()
        .filter(member -> DIRECTOR.equals(member.job()))
        .map(Crew::name)
        .distinct()
        .toList();
  }

  private List<CrewMember> keyCrew() {
    return credits.crew().stream()
        .filter(member -> KEY_JOBS.contains(member.job()))
        .collect(Collectors.groupingBy(Crew::id, LinkedHashMap::new, Collectors.toList()))
        .values()
        .stream()
        .map(
            jobs ->
                new CrewMember(
                    jobs.getFirst().name(), jobs.stream().map(Crew::job).distinct().toList()))
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
