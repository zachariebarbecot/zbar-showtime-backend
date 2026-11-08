package fr.zbar.showtime.backend.app.search.web;

import fr.zbar.showtime.backend.core.search.application.SeasonDetailView;
import fr.zbar.showtime.backend.core.search.domain.Episode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record SeasonDetailResponse(
    int seasonNumber,
    String name,
    String overview,
    LocalDate airDate,
    String posterUrl,
    List<EpisodeResponse> episodes) {

  static SeasonDetailResponse from(SeasonDetailView view) {
    return new SeasonDetailResponse(
        view.season().seasonNumber(),
        view.season().name(),
        view.season().overview(),
        view.season().airDate(),
        view.season().posterUrl(),
        view.season().episodes().stream()
            .map(episode -> EpisodeResponse.from(episode, view.watchedAtByEpisode()))
            .toList());
  }

  public record EpisodeResponse(
      int episodeNumber,
      String name,
      String overview,
      Integer runtime,
      LocalDate airDate,
      String stillUrl,
      boolean watched,
      Instant watchedAt) {

    static EpisodeResponse from(Episode episode, Map<Integer, Instant> watchedAtByEpisode) {
      return new EpisodeResponse(
          episode.episodeNumber(),
          episode.name(),
          episode.overview(),
          episode.runtime(),
          episode.airDate(),
          episode.stillUrl(),
          false,
          null);
    }
  }
}
