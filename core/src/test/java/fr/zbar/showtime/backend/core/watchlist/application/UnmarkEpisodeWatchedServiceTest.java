package fr.zbar.showtime.backend.core.watchlist.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistEntryNotFoundException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UnmarkEpisodeWatchedServiceTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final long SERIES_ID = 1396L;
  private static final Instant WATCHED_AT = Instant.parse("2026-04-02T08:00:00Z");

  private final List<WatchedEpisode> watched = new ArrayList<>();
  private final List<WatchlistSeries> series = new ArrayList<>();
  private final UnmarkEpisodeWatchedService service =
      new UnmarkEpisodeWatchedService(
          new InMemoryWatchedEpisodeRepository(watched),
          new InMemoryWatchlistSeriesRepository(series),
          Clock.fixed(WATCHED_AT, ZoneOffset.UTC));

  private void inWatchlist(SeriesStatus status) {
    series.add(
        new WatchlistSeries(UUID.randomUUID(), USER_ID, SERIES_ID, status, WATCHED_AT, null));
  }

  private WatchedEpisode viewing(UUID userId, int season, int episode) {
    return new WatchedEpisode(UUID.randomUUID(), userId, SERIES_ID, season, episode, WATCHED_AT);
  }

  @Test
  void should_delete_the_viewing_and_keep_the_others_when_the_episode_is_watched() {
    inWatchlist(SeriesStatus.WATCHING);
    WatchedEpisode other = viewing(USER_ID, 1, 1);
    watched.add(other);
    watched.add(viewing(USER_ID, 1, 2));

    service.execute(new UnmarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 2));

    assertThat(watched).containsExactly(other);
  }

  @Test
  void should_throw_not_found_when_the_episode_is_not_watched() {
    WatchedEpisode other = viewing(USER_ID, 1, 1);
    watched.add(other);

    assertThatThrownBy(
            () -> service.execute(new UnmarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 2)))
        .isInstanceOf(WatchlistEntryNotFoundException.class);

    assertThat(watched).containsExactly(other);
  }

  @Test
  void should_throw_not_found_and_keep_the_viewing_when_only_another_user_watched_the_episode() {
    WatchedEpisode other = viewing(OTHER_USER_ID, 1, 2);
    watched.add(other);

    assertThatThrownBy(
            () -> service.execute(new UnmarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 2)))
        .isInstanceOf(WatchlistEntryNotFoundException.class);

    assertThat(watched).containsExactly(other);
  }

  @Test
  void should_set_the_series_to_to_watch_when_the_last_viewing_is_deleted() {
    inWatchlist(SeriesStatus.WATCHING);
    watched.add(viewing(USER_ID, 1, 1));

    service.execute(new UnmarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 1));

    assertThat(series).extracting(WatchlistSeries::status).containsExactly(SeriesStatus.TO_WATCH);
  }

  @Test
  void should_keep_the_series_watching_when_other_viewings_remain() {
    inWatchlist(SeriesStatus.WATCHING);
    watched.add(viewing(USER_ID, 1, 1));
    watched.add(viewing(USER_ID, 1, 2));

    service.execute(new UnmarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 1));

    assertThat(series).extracting(WatchlistSeries::status).containsExactly(SeriesStatus.WATCHING);
  }

  @Test
  void should_keep_the_series_dropped_when_the_last_viewing_is_deleted() {
    inWatchlist(SeriesStatus.DROPPED);
    watched.add(viewing(USER_ID, 1, 1));

    service.execute(new UnmarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 1));

    assertThat(series).extracting(WatchlistSeries::status).containsExactly(SeriesStatus.DROPPED);
  }

  @Test
  void should_set_the_series_to_watching_when_a_regular_episode_is_unwatched_on_a_watched_series() {
    inWatchlist(SeriesStatus.WATCHED);
    watched.add(viewing(USER_ID, 1, 1));
    watched.add(viewing(USER_ID, 1, 2));

    service.execute(new UnmarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 2));

    assertThat(series).extracting(WatchlistSeries::status).containsExactly(SeriesStatus.WATCHING);
    assertThat(series).extracting(WatchlistSeries::watchedAt).containsOnlyNulls();
  }

  @Test
  void should_keep_the_series_watched_when_a_specials_episode_is_unwatched() {
    inWatchlist(SeriesStatus.WATCHED);
    watched.add(viewing(USER_ID, 1, 1));
    watched.add(viewing(USER_ID, 0, 1));

    service.execute(new UnmarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 0, 1));

    assertThat(series).extracting(WatchlistSeries::status).containsExactly(SeriesStatus.WATCHED);
  }
}
