package fr.zbar.showtime.backend.core.watchlist.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class WatchlistSeriesTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final Instant ADDED_AT = Instant.parse("2026-04-01T08:00:00Z");
  private static final Instant NOW = Instant.parse("2026-04-02T08:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
  private static final Clock LATER = Clock.fixed(NOW.plusSeconds(3600), ZoneOffset.UTC);

  private WatchlistSeries entry(SeriesStatus status, Instant watchedAt) {
    return new WatchlistSeries(
        UUID.fromString("22222222-2222-2222-2222-222222222222"),
        USER_ID,
        1399L,
        status,
        ADDED_AT,
        watchedAt);
  }

  @ParameterizedTest
  @EnumSource(
      value = SeriesStatus.class,
      names = {"TO_WATCH", "WATCHING"})
  void should_be_watched_at_the_current_instant_when_marked_as_watched(SeriesStatus from) {
    WatchlistSeries source = entry(from, null);

    WatchlistSeries watched = source.changeStatus(SeriesStatus.WATCHED, CLOCK);

    assertThat(watched.id()).isEqualTo(source.id());
    assertThat(watched.userId()).isEqualTo(USER_ID);
    assertThat(watched.tmdbId()).isEqualTo(1399L);
    assertThat(watched.status()).isEqualTo(SeriesStatus.WATCHED);
    assertThat(watched.addedAt()).isEqualTo(ADDED_AT);
    assertThat(watched.watchedAt()).isEqualTo(NOW);
  }

  @Test
  void should_keep_the_watched_date_when_a_watched_series_is_marked_as_watched_again() {
    WatchlistSeries watched = entry(SeriesStatus.WATCHED, NOW);

    WatchlistSeries again = watched.changeStatus(SeriesStatus.WATCHED, LATER);

    assertThat(again).isSameAs(watched);
    assertThat(again.watchedAt()).isEqualTo(NOW);
  }
}
