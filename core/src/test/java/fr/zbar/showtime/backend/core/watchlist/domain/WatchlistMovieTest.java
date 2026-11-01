package fr.zbar.showtime.backend.core.watchlist.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WatchlistMovieTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final Instant ADDED_AT = Instant.parse("2026-04-01T08:00:00Z");
  private static final Instant NOW = Instant.parse("2026-04-02T08:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
  private static final Clock LATER = Clock.fixed(NOW.plusSeconds(3600), ZoneOffset.UTC);

  private final WatchlistMovie toWatch =
      WatchlistMovie.add(USER_ID, 603L, Clock.fixed(ADDED_AT, ZoneOffset.UTC));

  @Test
  void should_be_watched_at_the_current_instant_when_marked_as_watched() {
    WatchlistMovie watched = toWatch.changeStatus(MovieStatus.WATCHED, CLOCK);

    assertThat(watched.id()).isEqualTo(toWatch.id());
    assertThat(watched.userId()).isEqualTo(USER_ID);
    assertThat(watched.tmdbId()).isEqualTo(603L);
    assertThat(watched.status()).isEqualTo(MovieStatus.WATCHED);
    assertThat(watched.addedAt()).isEqualTo(ADDED_AT);
    assertThat(watched.watchedAt()).isEqualTo(NOW);
  }

  @Test
  void should_clear_the_watched_date_when_a_watched_movie_goes_back_to_to_watch() {
    WatchlistMovie watched = toWatch.changeStatus(MovieStatus.WATCHED, CLOCK);

    WatchlistMovie back = watched.changeStatus(MovieStatus.TO_WATCH, LATER);

    assertThat(back.id()).isEqualTo(toWatch.id());
    assertThat(back.status()).isEqualTo(MovieStatus.TO_WATCH);
    assertThat(back.addedAt()).isEqualTo(ADDED_AT);
    assertThat(back.watchedAt()).isNull();
  }

  @Test
  void should_keep_the_watched_date_when_a_watched_movie_is_marked_as_watched_again() {
    WatchlistMovie watched = toWatch.changeStatus(MovieStatus.WATCHED, CLOCK);

    WatchlistMovie again = watched.changeStatus(MovieStatus.WATCHED, LATER);

    assertThat(again).isSameAs(watched);
    assertThat(again.watchedAt()).isEqualTo(NOW);
  }

  @Test
  void should_stay_unchanged_when_a_to_watch_movie_is_marked_as_to_watch() {
    assertThat(toWatch.changeStatus(MovieStatus.TO_WATCH, CLOCK)).isSameAs(toWatch);
  }
}
