package fr.zbar.showtime.backend.core.watchlist.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
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
}
