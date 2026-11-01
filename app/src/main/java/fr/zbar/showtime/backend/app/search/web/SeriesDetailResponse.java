package fr.zbar.showtime.backend.app.search.web;

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
    int numberOfSeasons,
    int numberOfEpisodes) {

  static SeriesDetailResponse from(SeriesDetail series) {
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
        series.numberOfEpisodes());
  }
}
