package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.domain.CastMember;
import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import java.time.LocalDate;
import java.util.List;

public record MovieDetailResponse(
    long id,
    String title,
    String originalTitle,
    String posterUrl,
    LocalDate releaseDate,
    Integer runtime,
    List<String> genres,
    String overview,
    MovieStatus status,
    List<String> directors,
    List<Crew> crew,
    List<Cast> cast) {

  static MovieDetailResponse from(MovieDetail movie) {
    return new MovieDetailResponse(
        movie.id(),
        movie.title(),
        movie.originalTitle(),
        movie.posterUrl(),
        movie.releaseDate(),
        movie.runtime(),
        movie.genres(),
        movie.overview(),
        movie.status(),
        movie.directors(),
        movie.crew().stream().map(Crew::from).toList(),
        movie.cast().stream().map(Cast::from).toList());
  }

  public record Crew(String name, List<String> jobs) {

    static Crew from(CrewMember member) {
      return new Crew(member.name(), member.jobs());
    }
  }

  public record Cast(String name, String character, String photoUrl) {

    static Cast from(CastMember member) {
      return new Cast(member.name(), member.character(), member.photoUrl());
    }
  }
}
