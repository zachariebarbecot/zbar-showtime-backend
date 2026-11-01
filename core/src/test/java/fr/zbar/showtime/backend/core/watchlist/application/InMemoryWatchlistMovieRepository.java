package fr.zbar.showtime.backend.core.watchlist.application;

import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovieRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InMemoryWatchlistMovieRepository implements WatchlistMovieRepository {

  private final List<WatchlistMovie> movies;

  public InMemoryWatchlistMovieRepository(List<WatchlistMovie> movies) {
    this.movies = movies;
  }

  @Override
  public void save(WatchlistMovie movie) {
    movies.removeIf(existing -> existing.id().equals(movie.id()));
    movies.add(movie);
  }

  @Override
  public Optional<WatchlistMovie> findByUserIdAndTmdbId(UUID userId, long tmdbId) {
    return movies.stream()
        .filter(movie -> movie.userId().equals(userId) && movie.tmdbId() == tmdbId)
        .findFirst();
  }

  @Override
  public List<WatchlistMovie> findByUserIdAndTmdbIdIn(UUID userId, Collection<Long> tmdbIds) {
    return movies.stream()
        .filter(movie -> movie.userId().equals(userId) && tmdbIds.contains(movie.tmdbId()))
        .toList();
  }
}
