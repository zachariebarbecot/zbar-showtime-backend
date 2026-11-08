package fr.zbar.showtime.backend.app.support;

import fr.zbar.showtime.backend.core.search.domain.SeasonSummary;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.util.List;

public final class SeriesDetails {

  private SeriesDetails() {}

  public static SeriesDetail returning(long id) {
    return withStatus(id, SeriesStatus.RETURNING);
  }

  public static SeriesDetail ended(long id, SeasonSummary... seasons) {
    return withStatus(id, SeriesStatus.ENDED, seasons);
  }

  private static SeriesDetail withStatus(long id, SeriesStatus status, SeasonSummary... seasons) {
    return new SeriesDetail(
        id,
        "Series",
        "Series",
        List.of(),
        null,
        null,
        status,
        null,
        null,
        null,
        null,
        List.of(),
        List.of(),
        List.of(),
        List.of(seasons));
  }
}
