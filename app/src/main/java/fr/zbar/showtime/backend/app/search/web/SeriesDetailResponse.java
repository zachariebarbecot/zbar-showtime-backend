package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.application.SeriesDetailView;
import fr.zbar.showtime.backend.core.search.domain.CastMember;
import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import fr.zbar.showtime.backend.core.search.domain.SeasonSummary;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.time.LocalDate;
import java.util.List;

public record SeriesDetailResponse(
    long id,
    String name,
    String originalName,
    String posterUrl,
    LocalDate firstAirDate,
    LocalDate lastAirDate,
    List<String> genres,
    String overview,
    SeriesStatus status,
    Integer numberOfSeasons,
    Integer numberOfEpisodes,
    List<String> creators,
    List<Crew> crew,
    List<Cast> cast,
    List<Season> seasons,
    String watchlistStatus) {

  static SeriesDetailResponse from(SeriesDetailView view) {
    SeriesDetail series = view.series();
    return new SeriesDetailResponse(
        series.id(),
        series.name(),
        series.originalName(),
        series.posterUrl(),
        series.firstAirDate(),
        series.lastAirDate(),
        series.genres(),
        series.overview(),
        series.status(),
        series.numberOfSeasons(),
        series.numberOfEpisodes(),
        series.creators(),
        series.crew().stream().map(Crew::from).toList(),
        series.cast().stream().map(Cast::from).toList(),
        series.seasons().stream().map(Season::from).toList(),
        view.watchlistStatus());
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

  public record Season(
      int seasonNumber, String name, int episodeCount, LocalDate airDate, String posterUrl) {

    static Season from(SeasonSummary season) {
      return new Season(
          season.seasonNumber(),
          season.name(),
          season.episodeCount(),
          season.airDate(),
          season.posterUrl());
    }
  }
}
