package fr.zbar.showtime.backend.app.watchlist.persistence;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.AlreadyInWatchlistException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class WatchlistSeriesPersistenceAdapter implements WatchlistSeriesRepository {

  private static final String UNIQUE_CONSTRAINT = "UC_watchlist_series_user_tmdb";

  private final WatchlistSeriesJpaRepository watchlistSeriesJpaRepository;
  private final UserJpaRepository userJpaRepository;

  @Override
  public void save(WatchlistSeries series) {
    try {
      watchlistSeriesJpaRepository.saveAndFlush(
          new WatchlistSeriesJpaEntity(
              series.id(),
              series.userId(),
              series.tmdbId(),
              series.status().name(),
              series.addedAt(),
              series.watchedAt()));
    } catch (DataIntegrityViolationException exception) {
      if (violatesUniqueness(exception)) {
        throw new AlreadyInWatchlistException();
      }
      throw exception;
    }
  }

  private static boolean violatesUniqueness(DataIntegrityViolationException exception) {
    return exception.getCause() instanceof ConstraintViolationException violation
        && UNIQUE_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName());
  }
}
