package fr.zbar.showtime.backend.app.watchlist.persistence;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.AlreadyInWatchlistException;
import fr.zbar.showtime.backend.core.watchlist.domain.MovieStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
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
class WatchlistMoviePersistenceAdapter implements WatchlistMovieRepository {

  private static final String UNIQUE_CONSTRAINT = "UC_watchlist_movie_user_tmdb";

  private final WatchlistMovieJpaRepository watchlistMovieJpaRepository;
  private final UserJpaRepository userJpaRepository;

  @Override
  public void save(WatchlistMovie movie) {
    try {
      watchlistMovieJpaRepository.saveAndFlush(
          new WatchlistMovieJpaEntity(
              movie.id(),
              userJpaRepository.getReferenceById(movie.userId()),
              movie.tmdbId(),
              movie.status().name(),
              movie.addedAt(),
              movie.watchedAt()));
    } catch (DataIntegrityViolationException exception) {
      if (violatesUniqueness(exception)) {
        throw new AlreadyInWatchlistException();
      }
      throw exception;
    }
  }

  @Override
  public Optional<WatchlistMovie> findByUserIdAndTmdbId(UUID userId, long tmdbId) {
    return watchlistMovieJpaRepository
        .findByUserIdAndTmdbId(userId, tmdbId)
        .map(WatchlistMoviePersistenceAdapter::toDomain);
  }

  @Override
  public List<WatchlistMovie> findByUserIdAndTmdbIdIn(UUID userId, Collection<Long> tmdbIds) {
    return List.of();
  }

  private static WatchlistMovie toDomain(WatchlistMovieJpaEntity entity) {
    return new WatchlistMovie(
        entity.getId(),
        entity.getUser().getId(),
        entity.getTmdbId(),
        MovieStatus.valueOf(entity.getStatus()),
        entity.getAddedAt(),
        entity.getWatchedAt());
  }

  private static boolean violatesUniqueness(DataIntegrityViolationException exception) {
    return exception.getCause() instanceof ConstraintViolationException violation
        && UNIQUE_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName());
  }
}
