package fr.zbar.showtime.backend.app.search.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zbar.showtime.backend.core.search.domain.CastMember;
import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class TmdbCredits {

  private static final int MAX_CAST = 15;

  private TmdbCredits() {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Actor(String name, String character, @JsonProperty("profile_path") String profilePath) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Contribution(long id, String name, String job) {}

  static List<CastMember> cast(List<Actor> actors, TmdbProperties properties) {
    return actors.stream()
        .limit(MAX_CAST)
        .map(
            actor ->
                new CastMember(
                    actor.name(),
                    actor.character(),
                    properties.imageUrl(properties.profileSize(), actor.profilePath())))
        .toList();
  }

  static List<CrewMember> keyCrew(List<Contribution> contributions, Set<String> keyJobs) {
    return contributions.stream()
        .filter(contribution -> keyJobs.contains(contribution.job()))
        .collect(Collectors.groupingBy(Contribution::id, LinkedHashMap::new, Collectors.toList()))
        .values()
        .stream()
        .map(
            jobs ->
                new CrewMember(
                    jobs.getFirst().name(),
                    jobs.stream().map(Contribution::job).distinct().toList()))
        .toList();
  }
}
