package fr.zbar.showtime.backend.app.watchlist.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaEntity;
import fr.zbar.showtime.backend.app.user.persistence.UserJpaRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.MovieStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import java.time.Instant;
import java.util.List;
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

  @Test
  void should_map_the_entries_of_the_user_found_for_the_requested_ids() {
    UUID userId = UUID.randomUUID();
    UserJpaEntity user = mock(UserJpaEntity.class);
    when(user.getId()).thenReturn(userId);
    UUID entryId = UUID.randomUUID();
    Instant addedAt = Instant.parse("2026-01-01T12:00:00Z");
    Instant watchedAt = Instant.parse("2026-02-01T12:00:00Z");
    when(jpaRepository.findByUserIdAndTmdbIdIn(userId, List.of(550L, 603L, 999L)))
        .thenReturn(
            List.of(
                new WatchlistMovieJpaEntity(entryId, user, 550L, "WATCHED", addedAt, watchedAt)));

    List<WatchlistMovie> found = adapter.findByUserIdAndTmdbIdIn(userId, List.of(550L, 603L, 999L));

    assertThat(found)
        .containsExactly(
            new WatchlistMovie(entryId, userId, 550L, MovieStatus.WATCHED, addedAt, watchedAt));
    assertThat(found.get(0).status()).isEqualTo(MovieStatus.WATCHED);
  }

  @Test
  void should_delete_the_entry_by_its_id() {
    UUID entryId = UUID.randomUUID();

    adapter.delete(
        new WatchlistMovie(
            entryId, UUID.randomUUID(), 550L, MovieStatus.TO_WATCH, Instant.now(), null));

    verify(jpaRepository).deleteById(entryId);
  }
}
