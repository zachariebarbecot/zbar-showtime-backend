package fr.zbar.showtime.backend.core.watchlist.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UnmarkEpisodeWatchedServiceTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final long SERIES_ID = 1396L;
  private static final Instant WATCHED_AT = Instant.parse("2026-04-02T08:00:00Z");

  private final List<WatchedEpisode> watched = new ArrayList<>();
  private final UnmarkEpisodeWatchedService service =
      new UnmarkEpisodeWatchedService(new InMemoryWatchedEpisodeRepository(watched));

  private WatchedEpisode viewing(UUID userId, int season, int episode) {
    return new WatchedEpisode(UUID.randomUUID(), userId, SERIES_ID, season, episode, WATCHED_AT);
  }

  @Test
  void should_delete_the_viewing_and_keep_the_others_when_the_episode_is_watched() {
    WatchedEpisode other = viewing(USER_ID, 1, 1);
    watched.add(other);
    watched.add(viewing(USER_ID, 1, 2));

    service.execute(new UnmarkEpisodeWatchedCommand(USER_ID, SERIES_ID, 1, 2));

    assertThat(watched).containsExactly(other);
  }
}
