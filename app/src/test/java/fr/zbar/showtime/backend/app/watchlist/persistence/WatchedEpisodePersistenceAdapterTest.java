package fr.zbar.showtime.backend.app.watchlist.persistence;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchedEpisode;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class WatchedEpisodePersistenceAdapterTest {

  private final WatchedEpisodeJpaRepository jpaRepository = mock(WatchedEpisodeJpaRepository.class);
  private final WatchedEpisodePersistenceAdapter adapter =
      new WatchedEpisodePersistenceAdapter(jpaRepository, mock(UserJpaRepository.class));
  private final WatchedEpisode episode =
      new WatchedEpisode(UUID.randomUUID(), UUID.randomUUID(), 1396L, 1, 2, Instant.now());

  private static DataIntegrityViolationException violationOf(String constraintName) {
    return new DataIntegrityViolationException(
        "violation",
        new ConstraintViolationException("violation", new SQLException(), constraintName));
  }

  @Test
  void should_ignore_the_violation_when_the_episode_is_already_watched() {
    when(jpaRepository.saveAndFlush(any()))
        .thenThrow(violationOf("UC_watched_episode_user_series_season_episode"));

    assertThatCode(() -> adapter.save(episode)).doesNotThrowAnyException();
  }

  @Test
  void should_rethrow_the_violation_when_it_is_not_the_episode_uniqueness() {
    DataIntegrityViolationException violation = violationOf("other_constraint");
    when(jpaRepository.saveAndFlush(any())).thenThrow(violation);

    assertThatThrownBy(() -> adapter.save(episode)).isSameAs(violation);
  }
}
