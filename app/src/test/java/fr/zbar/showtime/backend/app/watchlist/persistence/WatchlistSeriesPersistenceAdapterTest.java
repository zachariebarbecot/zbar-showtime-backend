package fr.zbar.showtime.backend.app.watchlist.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.dao.DataIntegrityViolationException;

class WatchlistSeriesPersistenceAdapterTest {

  private final WatchlistSeriesJpaRepository jpaRepository =
      mock(WatchlistSeriesJpaRepository.class);
  private final WatchlistSeriesPersistenceAdapter adapter =
      new WatchlistSeriesPersistenceAdapter(jpaRepository);

  @Test
  void should_rethrow_the_violation_when_it_is_not_the_user_and_tmdb_uniqueness() {
    DataIntegrityViolationException violation = new DataIntegrityViolationException("other");
    when(jpaRepository.saveAndFlush(ArgumentMatchers.any())).thenThrow(violation);
    WatchlistSeries series =
        new WatchlistSeries(
            UUID.randomUUID(),
            UUID.randomUUID(),
            1399L,
            SeriesStatus.TO_WATCH,
            Instant.now(),
            null);

    assertThatThrownBy(() -> adapter.save(series)).isSameAs(violation);
  }
}
