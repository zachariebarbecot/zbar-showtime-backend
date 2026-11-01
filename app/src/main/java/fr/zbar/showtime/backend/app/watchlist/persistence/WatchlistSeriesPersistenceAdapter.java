package fr.zbar.showtime.backend.app.watchlist.persistence;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.AlreadyInWatchlistException;
import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeriesRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
              userJpaRepository.getReferenceById(series.userId()),
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

  @Override
  public Optional<WatchlistSeries> findByUserIdAndTmdbId(UUID userId, long tmdbId) {
    return watchlistSeriesJpaRepository
        .findByUserIdAndTmdbId(userId, tmdbId)
        .map(WatchlistSeriesPersistenceAdapter::toDomain);
  }

  @Override
  public List<WatchlistSeries> findByUserIdAndTmdbIdIn(UUID userId, Collection<Long> tmdbIds) {
    return List.of();
  }

  private static WatchlistSeries toDomain(WatchlistSeriesJpaEntity entity) {
    return new WatchlistSeries(
        entity.getId(),
        entity.getUser().getId(),
        entity.getTmdbId(),
        SeriesStatus.valueOf(entity.getStatus()),
        entity.getAddedAt(),
        entity.getWatchedAt());
  }

  private static boolean violatesUniqueness(DataIntegrityViolationException exception) {
    return exception.getCause() instanceof ConstraintViolationException violation
        && UNIQUE_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName());
  }
}
