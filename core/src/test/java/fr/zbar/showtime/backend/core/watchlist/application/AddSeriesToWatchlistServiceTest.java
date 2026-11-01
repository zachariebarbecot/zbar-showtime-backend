package fr.zbar.showtime.backend.core.watchlist.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.search.application.InMemoryTitleCatalog;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AddSeriesToWatchlistServiceTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final Instant NOW = Instant.parse("2026-04-02T08:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private final List<WatchlistSeries> entries = new ArrayList<>();
  private final AddSeriesToWatchlistService service =
      new AddSeriesToWatchlistService(
          new InMemoryTitleCatalog(new CatalogItem(1399L, MediaType.TV, "GoT", null, null)),
          new InMemoryWatchlistSeriesRepository(entries),
          CLOCK);

  @Test
  void should_save_a_to_watch_entry_added_now_when_the_series_is_known() {
    WatchlistSeries added = service.execute(new AddSeriesToWatchlistCommand(USER_ID, 1399L));

    assertThat(entries).containsExactly(added);
    assertThat(added.userId()).isEqualTo(USER_ID);
    assertThat(added.tmdbId()).isEqualTo(1399L);
    assertThat(added.status()).isEqualTo(SeriesStatus.TO_WATCH);
    assertThat(added.addedAt()).isEqualTo(NOW);
    assertThat(added.watchedAt()).isNull();
  }

  @Test
  void should_save_nothing_when_the_series_is_unknown() {
    assertThatThrownBy(() -> service.execute(new AddSeriesToWatchlistCommand(USER_ID, 1L)))
        .isInstanceOf(TitleNotFoundException.class);

    assertThat(entries).isEmpty();
  }
}
