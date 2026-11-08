package fr.zbar.showtime.backend.app.support;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.util.List;

public final class SeriesDetails {

  private SeriesDetails() {}

  public static SeriesDetail returning(long id) {
    return new SeriesDetail(
        id,
        "Series",
        "Series",
        List.of(),
        null,
        null,
        SeriesStatus.RETURNING,
        null,
        null,
        null,
        null,
        List.of(),
        List.of(),
        List.of(),
        List.of());
  }
}
