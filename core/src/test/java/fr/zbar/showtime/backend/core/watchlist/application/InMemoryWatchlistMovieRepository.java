package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import java.util.List;

public class InMemoryWatchlistMovieRepository implements WatchlistMovieRepository {

  private final List<WatchlistMovie> movies;

  public InMemoryWatchlistMovieRepository(List<WatchlistMovie> movies) {
    this.movies = movies;
  }

  @Override
  public void save(WatchlistMovie movie) {
    movies.add(movie);
  }
}
