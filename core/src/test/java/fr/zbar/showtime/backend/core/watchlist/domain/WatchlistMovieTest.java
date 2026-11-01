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
}
