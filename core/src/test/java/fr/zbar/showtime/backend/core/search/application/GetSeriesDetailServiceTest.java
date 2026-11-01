package fr.zbar.showtime.backend.core.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import fr.zbar.showtime.backend.core.watchlist.application.InMemoryWatchlistSeriesRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
          List.of(),
          List.of());

  private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_USER = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private final List<WatchlistSeries> watchlist = new ArrayList<>();

  private final GetSeriesDetailService service =
      new GetSeriesDetailService(
          new InMemorySeriesDetailCatalog(BREAKING_BAD),
          new InMemoryWatchlistSeriesRepository(watchlist));

  private static WatchlistSeries inWatchlist(
      UUID user, long tmdbId, fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus status) {
    return new WatchlistSeries(UUID.randomUUID(), user, tmdbId, status, Instant.EPOCH, null);
  }

  @Test
  void should_return_the_series_detail_when_the_id_is_known() {
    assertThat(service.execute(new GetSeriesDetailCommand(USER, 1396L)).series())
        .isEqualTo(BREAKING_BAD);
  }

  @Test
  void should_expose_the_watchlist_status_of_the_caller_when_the_series_is_in_the_watchlist() {
    watchlist.add(
        inWatchlist(
            USER, 1396L, fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus.WATCHING));

    assertThat(service.execute(new GetSeriesDetailCommand(USER, 1396L)).watchlistStatus())
        .isEqualTo("WATCHING");
  }

  @Test
  void should_expose_no_watchlist_status_when_the_series_is_not_in_the_watchlist() {
    SeriesDetailView view = service.execute(new GetSeriesDetailCommand(USER, 1396L));

    assertThat(view.watchlistStatus()).isNull();
    assertThat(view.series()).isEqualTo(BREAKING_BAD);
  }

  @Test
  void should_expose_no_watchlist_status_when_only_another_user_has_the_series() {
    watchlist.add(
        inWatchlist(
            OTHER_USER,
            1396L,
            fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus.WATCHED));

    assertThat(service.execute(new GetSeriesDetailCommand(USER, 1396L)).watchlistStatus()).isNull();
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, -1L})
  void should_throw_invalid_field_without_calling_the_catalog_when_the_id_is_not_positive(
      long tmdbId) {
    GetSeriesDetailService unreachableCatalog =
        new GetSeriesDetailService(
            id -> {
              throw new AssertionError("catalog must not be called");
            },
            new InMemoryWatchlistSeriesRepository(watchlist));

    assertThatThrownBy(() -> unreachableCatalog.execute(new GetSeriesDetailCommand(USER, tmdbId)))
        .isInstanceOf(InvalidFieldException.class)
        .hasMessage("tmdbId: TMDB id must be greater than 0");
  }
}
