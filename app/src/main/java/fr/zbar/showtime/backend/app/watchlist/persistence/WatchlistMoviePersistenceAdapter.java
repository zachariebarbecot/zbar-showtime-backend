package fr.zbar.showtime.backend.app.watchlist.persistence;

import fr.zbar.showtime.backend.core.watchlist.domain.AlreadyInWatchlistException;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class WatchlistMoviePersistenceAdapter implements WatchlistMovieRepository {

  private final WatchlistMovieJpaRepository watchlistMovieJpaRepository;

  @Override
  public void save(WatchlistMovie movie) {
    try {
      watchlistMovieJpaRepository.saveAndFlush(
          new WatchlistMovieJpaEntity(
              movie.id(),
              movie.userId(),
              movie.tmdbId(),
              movie.status().name(),
              movie.addedAt(),
              movie.watchedAt()));
    } catch (DataIntegrityViolationException exception) {
      throw new AlreadyInWatchlistException();
    }
  }
}
