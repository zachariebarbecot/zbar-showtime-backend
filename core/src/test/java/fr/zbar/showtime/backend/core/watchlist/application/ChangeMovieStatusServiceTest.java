package fr.zbar.showtime.backend.core.watchlist.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.watchlist.domain.MovieStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChangeMovieStatusServiceTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final Instant NOW = Instant.parse("2026-04-02T08:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private final List<WatchlistMovie> entries = new ArrayList<>();
  private final ChangeMovieStatusService service =
      new ChangeMovieStatusService(new InMemoryWatchlistMovieRepository(entries), CLOCK);

  @Test
  void should_save_the_entry_as_watched_now_when_it_is_marked_as_watched() {
    entries.add(WatchlistMovie.add(USER_ID, 603L, CLOCK));

    WatchlistMovie changed =
        service.execute(new ChangeMovieStatusCommand(USER_ID, 603L, MovieStatus.WATCHED));

    assertThat(entries).containsExactly(changed);
    assertThat(changed.status()).isEqualTo(MovieStatus.WATCHED);
    assertThat(changed.watchedAt()).isEqualTo(NOW);
  }
}
