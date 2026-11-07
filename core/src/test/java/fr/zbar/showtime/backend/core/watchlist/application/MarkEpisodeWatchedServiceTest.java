package fr.zbar.showtime.backend.core.watchlist.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.search.application.InMemorySeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
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
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

class MarkEpisodeWatchedServiceTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final long SERIES_ID = 1396L;
  private static final Instant FIRST_MARK = Instant.parse("2026-04-02T08:00:00Z");
  private static final Instant SECOND_MARK = Instant.parse("2026-04-09T08:00:00Z");
  private static final SeasonDetail SEASON_1 =
      new SeasonDetail(
          1,
          "Saison 1",
          null,
          null,
          null,
          List.of(
              new Episode(1, "Pilote", null, null, null, null),
              new Episode(2, "Le chat", null, null, null, null)));

  private final List<WatchlistSeries> series = new ArrayList<>();
  private final List<WatchedEpisode> watched = new ArrayList<>();

  private MarkEpisodeWatchedService serviceAt(Instant now) {
    return new MarkEpisodeWatchedService(
        new InMemoryWatchlistSeriesRepository(series),
        new InMemorySeasonCatalog(SERIES_ID, SEASON_1),
        new InMemoryWatchedEpisodeRepository(watched),
        Clock.fixed(now, ZoneOffset.UTC));
  }

  private void inWatchlist(UUID userId) {
    series.add(
        new WatchlistSeries(
            UUID.randomUUID(), userId, SERIES_ID, SeriesStatus.TO_WATCH, FIRST_MARK, null));
  }

  @Test
  void
      should_save_a_viewing_dated_now_when_the_series_is_in_the_watchlist_and_the_episode_exists() {
    inWatchlist(USER_ID);

    serviceAt(FIRST_MARK).execute(new MarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 2));

    assertThat(watched).hasSize(1);
    WatchedEpisode saved = watched.getFirst();
    assertThat(saved.userId()).isEqualTo(USER_ID);
    assertThat(saved.seriesTmdbId()).isEqualTo(SERIES_ID);
    assertThat(saved.seasonNumber()).isEqualTo(1);
    assertThat(saved.episodeNumber()).isEqualTo(2);
    assertThat(saved.watchedAt()).isEqualTo(FIRST_MARK);
  }

  @Test
  void should_keep_one_viewing_with_its_original_date_when_the_episode_is_marked_again() {
    inWatchlist(USER_ID);
    serviceAt(FIRST_MARK).execute(new MarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 2));

    serviceAt(SECOND_MARK).execute(new MarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 2));

    assertThat(watched).hasSize(1);
    assertThat(watched.getFirst().watchedAt()).isEqualTo(FIRST_MARK);
  }

  @Test
  void should_save_nothing_when_the_series_is_not_in_the_watchlist() {
    inWatchlist(OTHER_USER_ID);

    assertThatThrownBy(
            () ->
                serviceAt(FIRST_MARK)
                    .execute(new MarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 1)))
        .isInstanceOf(WatchlistEntryNotFoundException.class);

    assertThat(watched).isEmpty();
  }

  @Test
  void should_save_nothing_when_the_season_is_unknown() {
    inWatchlist(USER_ID);

    assertThatThrownBy(
            () ->
                serviceAt(FIRST_MARK)
                    .execute(new MarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 7, 1)))
        .isInstanceOf(TitleNotFoundException.class);

    assertThat(watched).isEmpty();
  }

  @Test
  void should_save_nothing_when_the_episode_number_is_unknown() {
    inWatchlist(USER_ID);

    assertThatThrownBy(
            () ->
                serviceAt(FIRST_MARK)
                    .execute(new MarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 9)))
        .isInstanceOf(TitleNotFoundException.class);

    assertThat(watched).isEmpty();
  }

  @Test
  void should_save_a_viewing_for_each_user_when_both_mark_the_same_episode() {
    inWatchlist(USER_ID);
    inWatchlist(OTHER_USER_ID);
    serviceAt(FIRST_MARK).execute(new MarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 1));

    serviceAt(SECOND_MARK).execute(new MarkEpisodeWatchedCommand(OTHER_USER_ID, SERIES_ID, 1, 1));

    assertThat(watched)
        .extracting(WatchedEpisode::userId, WatchedEpisode::watchedAt)
        .containsExactlyInAnyOrder(
            Tuple.tuple(USER_ID, FIRST_MARK), Tuple.tuple(OTHER_USER_ID, SECOND_MARK));
  }
}
