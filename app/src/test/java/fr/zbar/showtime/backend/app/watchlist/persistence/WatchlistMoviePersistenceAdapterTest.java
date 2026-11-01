package fr.zbar.showtime.backend.app.watchlist.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.MovieStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.dao.DataIntegrityViolationException;

class WatchlistMoviePersistenceAdapterTest {

  private final WatchlistMovieJpaRepository jpaRepository = mock(WatchlistMovieJpaRepository.class);
  private final WatchlistMoviePersistenceAdapter adapter =
      new WatchlistMoviePersistenceAdapter(jpaRepository, mock(UserJpaRepository.class));

  @Test
  void should_rethrow_the_violation_when_it_is_not_the_user_and_tmdb_uniqueness() {
    DataIntegrityViolationException violation = new DataIntegrityViolationException("other");
    when(jpaRepository.saveAndFlush(ArgumentMatchers.any())).thenThrow(violation);
    WatchlistMovie movie =
        new WatchlistMovie(
            UUID.randomUUID(), UUID.randomUUID(), 603L, MovieStatus.TO_WATCH, Instant.now(), null);

    assertThatThrownBy(() -> adapter.save(movie)).isSameAs(violation);
  }
}
