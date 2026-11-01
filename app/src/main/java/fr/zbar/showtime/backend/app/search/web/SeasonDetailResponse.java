package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import java.time.LocalDate;
import java.util.List;

public record SeasonDetailResponse(
    int seasonNumber,
    String name,
    String overview,
    LocalDate airDate,
    String posterUrl,
    List<EpisodeResponse> episodes) {

  static SeasonDetailResponse from(SeasonDetail season) {
    return new SeasonDetailResponse(
        season.seasonNumber(),
        season.name(),
        season.overview(),
        season.airDate(),
        season.posterUrl(),
        season.episodes().stream().map(EpisodeResponse::from).toList());
  }

  public record EpisodeResponse(
      int episodeNumber,
      String name,
      String overview,
      Integer runtime,
      LocalDate airDate,
      String stillUrl) {

    static EpisodeResponse from(Episode episode) {
      return new EpisodeResponse(
          episode.episodeNumber(),
          episode.name(),
          episode.overview(),
          episode.runtime(),
          episode.airDate(),
          episode.stillUrl());
    }
  }
}
