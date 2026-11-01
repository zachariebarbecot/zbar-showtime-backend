package fr.zbar.showtime.backend.core.watchlist.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistEntryNotFoundException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RemoveSeriesFromWatchlistServiceTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-04-02T08:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private final List<WatchlistSeries> entries = new ArrayList<>();
  private final RemoveSeriesFromWatchlistService service =
      new RemoveSeriesFromWatchlistService(new InMemoryWatchlistSeriesRepository(entries));

  @Test
  void should_remove_the_entry_when_it_is_in_the_watchlist_whatever_its_status() {
    entries.add(WatchlistSeries.add(USER_ID, 1399L, CLOCK));
    entries.add(
        new WatchlistSeries(UUID.randomUUID(), USER_ID, 1396L, SeriesStatus.WATCHED, NOW, NOW));

    service.execute(new RemoveSeriesFromWatchlistCommand(USER_ID, 1399L));
    service.execute(new RemoveSeriesFromWatchlistCommand(USER_ID, 1396L));

    assertThat(entries).isEmpty();
  }

  @Test
  void should_throw_not_found_when_the_series_is_not_in_the_watchlist() {
    assertThatThrownBy(() -> service.execute(new RemoveSeriesFromWatchlistCommand(USER_ID, 1399L)))
        .isInstanceOf(WatchlistEntryNotFoundException.class)
        .hasMessage("Watchlist entry not found");
  }

  @Test
  void should_keep_the_entry_of_another_user_when_the_series_is_not_in_my_watchlist() {
    WatchlistSeries other = WatchlistSeries.add(OTHER_USER_ID, 1399L, CLOCK);
    entries.add(other);

    assertThatThrownBy(() -> service.execute(new RemoveSeriesFromWatchlistCommand(USER_ID, 1399L)))
        .isInstanceOf(WatchlistEntryNotFoundException.class);

    assertThat(entries).containsExactly(other);
  }

  @Test
  void should_keep_the_entry_of_the_other_user_when_both_have_the_series() {
    WatchlistSeries other = WatchlistSeries.add(OTHER_USER_ID, 1399L, CLOCK);
    entries.add(WatchlistSeries.add(USER_ID, 1399L, CLOCK));
    entries.add(other);

    service.execute(new RemoveSeriesFromWatchlistCommand(USER_ID, 1399L));

    assertThat(entries).containsExactly(other);
  }
}
