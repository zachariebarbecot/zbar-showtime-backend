package fr.zbar.showtime.backend.core.watchlist.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.watchlist.domain.MovieStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistEntryNotFoundException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RemoveMovieFromWatchlistServiceTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-04-02T08:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private final List<WatchlistMovie> entries = new ArrayList<>();
  private final RemoveMovieFromWatchlistService service =
      new RemoveMovieFromWatchlistService(new InMemoryWatchlistMovieRepository(entries));

  @Test
  void should_remove_the_entry_when_it_is_in_the_watchlist_whatever_its_status() {
    entries.add(WatchlistMovie.add(USER_ID, 550L, CLOCK));
    entries.add(
        new WatchlistMovie(UUID.randomUUID(), USER_ID, 603L, MovieStatus.WATCHED, NOW, NOW));

    service.execute(new RemoveMovieFromWatchlistCommand(USER_ID, 550L));
    service.execute(new RemoveMovieFromWatchlistCommand(USER_ID, 603L));

    assertThat(entries).isEmpty();
  }

  @Test
  void should_throw_not_found_when_the_movie_is_not_in_the_watchlist() {
    assertThatThrownBy(() -> service.execute(new RemoveMovieFromWatchlistCommand(USER_ID, 550L)))
        .isInstanceOf(WatchlistEntryNotFoundException.class)
        .hasMessage("Watchlist entry not found");
  }

  @Test
  void should_keep_the_entry_of_another_user_when_the_movie_is_not_in_my_watchlist() {
    WatchlistMovie other = WatchlistMovie.add(OTHER_USER_ID, 550L, CLOCK);
    entries.add(other);

    assertThatThrownBy(() -> service.execute(new RemoveMovieFromWatchlistCommand(USER_ID, 550L)))
        .isInstanceOf(WatchlistEntryNotFoundException.class);

    assertThat(entries).containsExactly(other);
  }

  @Test
  void should_keep_the_entry_of_the_other_user_when_both_have_the_movie() {
    WatchlistMovie other = WatchlistMovie.add(OTHER_USER_ID, 550L, CLOCK);
    entries.add(WatchlistMovie.add(USER_ID, 550L, CLOCK));
    entries.add(other);

    service.execute(new RemoveMovieFromWatchlistCommand(USER_ID, 550L));

    assertThat(entries).containsExactly(other);
  }
}
