package fr.zbar.showtime.backend.core.search.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetSeriesDetailServiceTest {

  private static final SeriesDetail BREAKING_BAD =
      new SeriesDetail(
          1396L,
          "Breaking Bad",
          "Breaking Bad",
          List.of("Drame"),
          LocalDate.of(2008, 1, 20),
          LocalDate.of(2013, 9, 29),
          SeriesStatus.ENDED,
          "Un professeur de chimie devient fabricant de drogue.",
          "https://image.tmdb.org/t/p/w342/b.jpg",
          5,
          62,
          List.of("Vince Gilligan"),
          List.of(),
          List.of());

  private final GetSeriesDetailService service =
      new GetSeriesDetailService(new InMemorySeriesDetailCatalog(BREAKING_BAD));

  @Test
  void should_return_the_series_detail_when_the_id_is_known() {
    assertThat(service.execute(new GetSeriesDetailCommand(1396L))).isEqualTo(BREAKING_BAD);
  }
}
