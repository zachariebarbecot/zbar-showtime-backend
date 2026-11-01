package fr.zbar.showtime.backend.app.watchlist.persistence;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import org.springframework.stereotype.Component;

@Component
class WatchlistMoviePersistenceAdapter implements WatchlistMovieRepository {

  @Override
  public void save(WatchlistMovie movie) {}
}
