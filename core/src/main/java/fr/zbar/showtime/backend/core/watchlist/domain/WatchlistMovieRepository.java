package fr.zbar.showtime.backend.core.watchlist.domain;

import java.util.Optional;
import java.util.UUID;

public interface WatchlistMovieRepository {

  void save(WatchlistMovie movie);

  Optional<WatchlistMovie> findByUserIdAndTmdbId(UUID userId, long tmdbId);
}
