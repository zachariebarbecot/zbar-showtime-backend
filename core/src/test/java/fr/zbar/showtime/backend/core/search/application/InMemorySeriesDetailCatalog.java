package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.util.List;

public class InMemorySeriesDetailCatalog implements SeriesDetailCatalog {

  private final List<SeriesDetail> series;

  public InMemorySeriesDetailCatalog(SeriesDetail... series) {
    this.series = List.of(series);
  }

  @Override
  public SeriesDetail findSeriesDetail(long id) {
    return series.stream()
        .filter(candidate -> candidate.id() == id)
        .findFirst()
        .orElseThrow(TitleNotFoundException::new);
  }
}
